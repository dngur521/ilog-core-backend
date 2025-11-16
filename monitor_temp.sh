#!/bin/bash

# ===================================================
# 1. CPU 온도 및 사용률 확인
# ===================================================
CPU_TEMP=$(sudo vcgencmd measure_temp | cut -f 2 -d "=" | cut -f 1 -d "'")
CPU_IDLE=$(top -bn1 | grep "Cpu(s)" | awk '{printf("%d", $8)}')
CPU_USAGE=$((100 - CPU_IDLE))

# ===================================================
# 2. SSD SMART 정보 확인 및 정리
# ===================================================
SSD_TEMP=$(sudo nvme smart-log /dev/nvme0 | grep -E 'temp|Temp' | awk '{print $3 " " $4}' | cut -d '(' -f 1 | head -n 1)

# ===================================================
# 3. 메모리 사용량 확인 및 계산
# ===================================================
MEM_TOTAL=$(free -m | awk 'NR==2{print $2}')
MEM_AVAILABLE=$(free -m | awk 'NR==2{print $7}')
MEM_REAL_USED=$((MEM_TOTAL - MEM_AVAILABLE))
MEM_PERCENT=$(echo "scale=1; (${MEM_TOTAL} - ${MEM_AVAILABLE}) * 100 / ${MEM_TOTAL}" | bc)

# ===================================================
# 4. 네트워크 트래픽 (MB/s) 및 디스크 I/O (MB/s) 확인
# ===================================================

# A. 디스크 I/O (iostat 사용)
IOSTAT_DATA=$(iostat -d 1 2 | grep 'nvme0n1' | tail -n 1)
DISK_READ_KB=$(echo "$IOSTAT_DATA" | awk '{print $4}')
DISK_WRITE_KB=$(echo "$IOSTAT_DATA" | awk '{print $5}')

# B. 네트워크 트래픽 (sar -n DEV 사용: eth0 인터페이스)
NET_DATA=$(sar -n DEV 1 2 | grep 'eth0' | tail -n 1)
NET_RX_KB=$(echo "$NET_DATA" | awk '{print $5}')
NET_TX_KB=$(echo "$NET_DATA" | awk '{print $6}')

# C. MB/s 계산 후 소수점 2자리 포맷 강제 적용 (핵심 수정)
# 계산 결과가 0일지라도 0.00으로 표시되도록 printf 사용

DISK_READ_MB=$(printf "%.2f" $(echo "scale=2; ${DISK_READ_KB} / 1024" | bc))
DISK_WRITE_MB=$(printf "%.2f" $(echo "scale=2; ${DISK_WRITE_KB} / 1024" | bc))

NET_DOWNLOAD_MB=$(printf "%.2f" $(echo "scale=2; ${NET_RX_KB} / 1024" | bc))
NET_UPLOAD_MB=$(printf "%.2f" $(echo "scale=2; ${NET_TX_KB} / 1024" | bc))

# ===================================================
# 5. 결과 출력
# ===================================================
echo "=========================================="
echo "         Raspberry Pi Monitoring"
echo "=========================================="
echo "CPU Temp: ${CPU_TEMP}C | Usage: ${CPU_USAGE}%"
echo "------------------------------------------"
echo "SSD Temperature: (/dev/nvme0): ${SSD_TEMP}"
echo "------------------------------------------"
echo "Memory Usage: ${MEM_REAL_USED}MB / ${MEM_TOTAL}MB (${MEM_PERCENT}%)"
echo "------------------------------------------"
echo "Network  (MB/s): Down: ${NET_DOWNLOAD_MB} | Upload: ${NET_UPLOAD_MB}"
echo "Disk I/O (MB/s): Read: ${DISK_READ_MB} | Write : ${DISK_WRITE_MB}"
echo "=========================================="