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
pl_line=$(grep -E 'mvn deploy -pl' "$PUBLISH_WORKFLOW" | grep -oE '\-pl [^ ]+' | head -n 1 || true)
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

if [ "$FAILED" -ne 0 ]; then
    echo ""
    echo "❌ 模块清单一致性校验失败，请同步更新父 pom、BOM 与 .github/workflows/publish-parent.yml"
    exit 1
fi

echo "🎉 模块数与发布清单数一致（父 pom / BOM / 发布 -pl / Summary 四方对齐）"
