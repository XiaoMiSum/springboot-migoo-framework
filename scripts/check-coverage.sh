#!/usr/bin/env bash
#
# 聚合覆盖率门禁：全仓行覆盖率 ≥ 阈值 才允许合入
#
# 背景：JaCoCo check 按单模块卡点（migoo-framework-parent/pom.xml，底线 50%），
# 但单模块底线无法阻止「删测试但仍在底线上」的整体缓慢劣化；本脚本对全仓 CSV 汇总后再卡一道：
#
#   - 总行覆盖率（LINE_COVERED / (LINE_COVERED + LINE_MISSED)）≥ ${MIN_LINE_COVERAGE}
#
# 用法（需先 mvn verify 生成 target/site/jacoco/jacoco.csv）：
#   bash scripts/check-coverage.sh            # 默认门槛 75%
#   MIN_LINE_COVERAGE=76 bash scripts/check-coverage.sh
#
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
MIN_LINE_COVERAGE=${MIN_LINE_COVERAGE:-75}

covered=$(find "$ROOT/migoo-framework-parent" -name jacoco.csv -not -path '*/target/classes/*' 2>/dev/null | wc -l | tr -d ' ')
if [ "$covered" -eq 0 ]; then
    echo "❌ 未找到任何 jacoco.csv，请先执行: mvn clean verify -Dgpg.skip=true"
    exit 1
fi

total=$(find "$ROOT/migoo-framework-parent" -name jacoco.csv 2>/dev/null -exec cat {} + | awk -F, '
    $1 ~ /GROUP/ { next }
    { line_missed += $8; line_covered += $9 }
    END { print line_covered + line_missed }')

covered_lines=$(find "$ROOT/migoo-framework-parent" -name jacoco.csv 2>/dev/null -exec cat {} + | awk -F, '
    $1 ~ /GROUP/ { next }
    { line_covered += $9 }
    END { print line_covered }')

if [ -z "$total" ] || [ "$total" -eq 0 ]; then
    echo "❌ jacoco.csv 解析失败（无统计行）"
    exit 1
fi

ratio=$(awk -v c="$covered_lines" -v t="$total" 'BEGIN { printf "%.2f", c * 100 / t }')
echo "📊 全仓行覆盖率: ${ratio}%（覆盖 ${covered_lines}/${total} 行，门槛 ${MIN_LINE_COVERAGE}%）"

ok=$(awk -v r="$ratio" -v m="$MIN_LINE_COVERAGE" 'BEGIN { print (r >= m) ? 1 : 0 }')
if [ "$ok" -ne 1 ]; then
    echo "❌ 行覆盖率低于门槛 ${MIN_LINE_COVERAGE}%：请补测试，或在确认合理后显式调整门槛（需在 PR 中说明）"
    exit 1
fi

echo "🎉 聚合覆盖率门禁通过（${ratio}% ≥ ${MIN_LINE_COVERAGE}%）"
