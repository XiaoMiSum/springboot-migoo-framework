#!/usr/bin/env bash
#
# 校验「模块数 vs 发布清单数」一致性（docs/observability.md §10 发布清单）
#
# 背景：新增模块若只进父 pom <modules> 而漏更新发布清单，用户将引不到
# （websocket 曾历史遗漏）。本脚本在发布 workflow 中前置执行，任一不一致即失败：
#
#   1. 父 pom <modules>  ↔  publish-parent.yml 的 mvn deploy -pl 清单（双向）
#   2. 父 pom <modules>  ↔  BOM migoo-framework-dependencies 的 dependencyManagement（双向）
#   3. publish-parent.yml 的「📦 Modules:」汇总行是否逐个列出所有模块（短名或全名）
#   4. 全仓 pom 字面版本号 / migoo.framework.version 属性 / CHANGELOG.md 条目三方一致
#      （examples/ 示例工程独立版本 1.0.0-SNAPSHOT，不参与发布版本校验）
#
# 用法（本地或 CI）：bash scripts/check-publish-modules.sh
#
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
PARENT_POM="$ROOT/migoo-framework-parent/pom.xml"
BOM_POM="$ROOT/migoo-framework-dependencies/pom.xml"
PUBLISH_WORKFLOW="$ROOT/.github/workflows/publish-parent.yml"

FAILED=0

fail() {
    echo "❌ $1"
    shift
    for line in "$@"; do
        echo "   $line"
    done
    FAILED=1
}

for file in "$PARENT_POM" "$BOM_POM" "$PUBLISH_WORKFLOW"; do
    if [ ! -f "$file" ]; then
        fail "缺少文件: $file"
    fi
done

work_dir=$(mktemp -d)
trap 'rm -rf "$work_dir"' EXIT

# 1) 父 pom <module> 列表
grep -oE '<module>[^<]+</module>' "$PARENT_POM" \
    | sed -E 's|.*<module>([^<]+)</module>.*|\1|' \
    | sort > "$work_dir/modules"

# 2) publish-parent.yml 的 mvn deploy -pl 清单（去掉 migoo-framework-parent/ 前缀）
pl_line=$(grep -E 'mvnw? deploy -pl' "$PUBLISH_WORKFLOW" | grep -oE '\-pl [^ ]+' | head -n 1 || true)
if [ -z "$pl_line" ]; then
    fail "publish-parent.yml 未找到 mvn deploy -pl 发布清单"
else
    echo "$pl_line" \
        | sed 's/-pl //' \
        | tr ',' '\n' \
        | sed 's|migoo-framework-parent/||' \
        | sed '/^[[:space:]]*$/d' \
        | sort > "$work_dir/publish_pl"
    echo "✅ 父 pom modules ($(wc -l < "$work_dir/modules" | tr -d ' ') 个) vs 发布 -pl 清单 ($(wc -l < "$work_dir/publish_pl" | tr -d ' ') 个)"

    missing=$(comm -23 "$work_dir/modules" "$work_dir/publish_pl" || true)
    extra=$(comm -13 "$work_dir/modules" "$work_dir/publish_pl" || true)
    if [ -n "$missing" ]; then
        fail "以下模块在父 pom <modules> 中，但未出现在 publish-parent.yml 的 -pl 清单（用户将引不到）:"
        while IFS= read -r line; do echo "     - $line"; FAILED=1; done <<< "$missing"
    fi
    if [ -n "$extra" ]; then
        fail "以下模块在 -pl 清单中，但父 pom <modules> 不存在（发布会失败）:"
        while IFS= read -r line; do echo "     - $line"; FAILED=1; done <<< "$extra"
    fi
fi

# 3) BOM dependencyManagement 中的 starter 条目
grep -oE '<artifactId>migoo-spring-boot-starter-[^<]+</artifactId>' "$BOM_POM" \
    | sed -E 's|.*>(migoo-spring-boot-starter-[^<]+)<.*|\1|' \
    | sort -u > "$work_dir/bom"

echo "✅ 父 pom modules vs BOM dependencyManagement ($(wc -l < "$work_dir/bom" | tr -d ' ') 个)"

missing=$(comm -23 "$work_dir/modules" "$work_dir/bom" || true)
extra=$(comm -13 "$work_dir/modules" "$work_dir/bom" || true)
if [ -n "$missing" ]; then
    fail "以下模块未在 BOM migoo-framework-dependencies 中声明 dependencyManagement（用户 import BOM 后引不到）:"
    while IFS= read -r line; do echo "     - $line"; FAILED=1; done <<< "$missing"
fi
if [ -n "$extra" ]; then
    fail "以下模块在 BOM 中声明，但父 pom <modules> 不存在:"
    while IFS= read -r line; do echo "     - $line"; FAILED=1; done <<< "$extra"
fi

# 4) 发布 Summary 的「📦 Modules:」行逐个覆盖所有模块（token 允许短名或全名）
summary_line=$(grep -E '📦 Modules:' "$PUBLISH_WORKFLOW" | head -n 1 || true)
if [ -z "$summary_line" ]; then
    fail "publish-parent.yml 未找到「📦 Modules:」汇总行"
else
    while IFS= read -r module; do
        short=${module#migoo-spring-boot-starter-}
        found=false
        for token in $(echo "$summary_line" | sed 's/.*Modules://' | tr ',' ' ' | tr -d '"`'); do
            if [ "$token" = "$module" ] || [ "$token" = "$short" ]; then
                found=true
                break
            fi
        done
        if [ "$found" = false ]; then
            fail "「📦 Modules:」汇总行缺少模块: ${module}（发布完成后提示会误导排查）"
        fi
    done < "$work_dir/modules"
fi

# 5) 版本号一致性：发布范围（除 examples/）的 pom 字面 <version> / migoo.framework.version 属性 / CHANGELOG 必须同一版本
#    背景：版本号在全仓硬编码，漏改任一处会导致用户引到 404 或父子 pom 解析失败
root_version=$(grep -oE '<version>[^$<]+</version>' "$ROOT/pom.xml" | head -n 1 | sed -E 's|.*<version>([^<]+)</version>.*|\1|')

if [ -z "$root_version" ]; then
    fail "根 pom.xml 未解析到版本号"
else
    echo "基准版本: $root_version"

    while IFS= read -r pom; do
        while IFS= read -r v; do
            if [ "$v" != "$root_version" ]; then
                fail "版本号不一致: $pom 中为 ${v}（根 pom 为 ${root_version}）"
            fi
        done < <(grep -oE '<version>[^$<]+</version>' "$pom" | sed -E 's|.*<version>([^<]+)</version>.*|\1|')
    done < <(find "$ROOT" -name pom.xml -not -path '*/target/*' -not -path "$ROOT/examples/*" | sort)

    # BOM / 父 pom 的 migoo.framework.version 属性
    for pom in "$BOM_POM" "$PARENT_POM"; do
        prop=$(grep -oE '<migoo\.framework\.version>[^<]+</migoo\.framework\.version>' "$pom" \
            | sed -E 's|.*>([^<]+)<.*|\1|' || true)
        if [ -z "$prop" ]; then
            fail "$pom 未声明 migoo.framework.version 属性"
        elif [ "$prop" != "$root_version" ]; then
            fail "$pom 的 migoo.framework.version=${prop}，与根 pom $root_version 不一致"
        fi
    done

    # CHANGELOG 必须有当前版本条目（否则本次发布变更无处可查）
    if [ ! -f "$ROOT/CHANGELOG.md" ]; then
        fail "缺少 CHANGELOG.md（发布需记录变更）"
    elif ! grep -qE "^#+ *.*$root_version" "$ROOT/CHANGELOG.md"; then
        fail "CHANGELOG.md 未包含当前版本 $root_version 的条目"
    fi
fi

if [ "$FAILED" -ne 0 ]; then
    echo ""
    echo "❌ 模块清单一致性校验失败，请同步更新父 pom、BOM 与 .github/workflows/publish-parent.yml"
    exit 1
fi

echo "🎉 模块数与发布清单数一致（父 pom / BOM / 发布 -pl / Summary 四方对齐），版本号全仓一致（${root_version}）"
