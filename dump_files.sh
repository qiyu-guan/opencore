#!/bin/sh

# ============================================
# 超屌的文件导出脚本 - 彩色进度条 + 格式输出
# 用法: ./dump_files.sh [目标目录]
# 默认目录: /storage/emulated/0/Download
# ============================================

# 颜色定义 (仅用于 stderr 进度提示)
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

# 默认下载目录 (Termux 中外部存储的典型路径)
DEFAULT_DIR="/storage/emulated/0/Download"
# 如果默认目录不存在，尝试备用路径
if [ ! -d "$DEFAULT_DIR" ]; then
    DEFAULT_DIR="/sdcard/Download"
fi

# 获取目标目录
target_dir="${1:-$DEFAULT_DIR}"

# 检查目录是否存在
if [ ! -d "$target_dir" ]; then
    echo -e "${RED}错误：目录 '$target_dir' 不存在${RESET}" >&2
    exit 1
fi

# 临时文件: 存储所有文件列表 (null 分隔)
tmp_list=$(mktemp)
trap 'rm -f "$tmp_list"; exit' INT TERM EXIT

# 收集所有普通文件 (递归)
echo -e "${CYAN}🔍 正在扫描目录: $target_dir ...${RESET}" >&2
find "$target_dir" -type f -print0 > "$tmp_list"

# 统计文件总数 (处理 null 分隔符，忽略空行)
total=$(tr '\0' '\n' < "$tmp_list" | wc -l)
if [ "$total" -eq 0 ]; then
    echo -e "${YELLOW}⚠️ 没有找到任何普通文件。${RESET}" >&2
    exit 0
fi

echo -e "${GREEN}✅ 发现 $total 个文件，开始导出...${RESET}\n" >&2

# 进度条函数 (输出到 stderr)
# 参数: current, total, filename
draw_progress() {
    cur=$1
    tot=$2
    fname="$3"
    percent=$((cur * 100 / tot))
    # 进度条长度 (40个字符)
    bar_len=40
    filled=$((percent * bar_len / 100))
    empty=$((bar_len - filled))
    
    # 构建进度条块
    bar=""
    i=0
    while [ $i -lt $filled ]; do
        bar="${bar}#"
        i=$((i + 1))
    done
    while [ $i -lt $bar_len ]; do
        bar="${bar}-"
        i=$((i + 1))
    done
    
    # 截断文件名 (终端宽度一般 80，留出足够空间)
    max_name_len=40
    if [ ${#fname} -gt $max_name_len ]; then
        fname="...${fname#${fname%???}}"
    fi
    
    # 彩色输出进度条到 stderr，使用 \r 覆盖当前行
    printf "\r${GREEN}[${CYAN}%s${GREEN}] ${YELLOW}%3d%%${RESET}  ${BOLD}${BLUE}%s${RESET}  (%-5d/%-5d)" \
        "$bar" "$percent" "$fname" "$cur" "$tot" >&2
}

# 主循环: 读取文件列表并处理
count=0
while IFS= read -r -d '' file; do
    count=$((count + 1))
    
    # 仅提取文件名 (不包含路径) 用于进度条显示
    base_name=$(basename "$file")
    draw_progress "$count" "$total" "$base_name"
    
    # 输出文件完整路径 (stdout)
    printf "%s\n" "$file"
    # 输出文件内容 (stdout)
    cat "$file"
    # 文件分隔空行
    printf "\n"
done < "$tmp_list"

# 换行，避免进度条被覆盖
printf "\n\n" >&2
echo -e "${GREEN}✨ 导出完成！共处理 $total 个文件。${RESET}" >&2

rm -f "$tmp_list"
trap - INT TERM EXIT
