#!/bin/bash

# 필요한 패키지: sysstat, bc, smartmontools, lm-sensors 설치 확인 필요

# ===================================================
# 0. 장치 이름 정의
# ===================================================
# lsblk 결과에 따라 장치 이름을 정의합니다.
DEV_SSD="sda" # 시스템 디스크 (SSD)
DEV_HDD="sdb" # 데이터 디스크 (HDD)
NETWORK_INTERFACE="enp4s0" # 현재 시스템의 네트워크 인터페이스 이름으로 수정하세요 (예: ens33, enp0s3)

# ===================================================
# 헬퍼 함수: bc를 사용하여 KB를 MB로 변환
# ===================================================
calculate_io_mb() {
    local KB=$1
    # KB 값이 없거나 비어있으면 0.00을 반환하여 bc 구문 오류 방지
    if [ -z "$KB" ] || [ "$KB" == "0.00" ]; then
        echo "0.00"
    else
        # bc를 사용하여 소수점 계산 및 포맷
        printf "%.2f" $(echo "scale=2; ${KB} / 1024" | bc)
    fi
}

# ===================================================
# 헬퍼 함수: smartctl을 사용하여 디스크 온도 추출
# ===================================================
get_disk_temp() {
    local DEVICE=$1
    local TEMP="N/A (jq/smartctl fail)"

    # jq 설치 확인
    if ! command -v jq &> /dev/null; then
        echo "N/A (jq missing)"
        return
    fi

    # smartctl 실행 및 JSON 출력 받기
    local SMART_JSON=$(sudo smartctl -A -j /dev/${DEVICE} 2>/dev/null)
    
    # JSON에서 temperature/current 필드 추출 시도
    # 대부분의 디스크는 'temperature/current' 경로에 온도가 있습니다.
    TEMP_RAW=$(echo "$SMART_JSON" | jq '.temperature.current' 2>/dev/null)

    # 추출이 실패하거나 null일 경우, SMART Attributes의 194번 속성에서 추출 시도
    if [ -z "$TEMP_RAW" ] || [ "$TEMP_RAW" == "null" ]; then
        # ID 194의 raw_value 필드 추출
        TEMP_RAW=$(echo "$SMART_JSON" | jq '.ata_smart_attributes.table[] | select(.id == 194) | .raw.value' 2>/dev/null)
    fi

    # 최종 결과 확인
    if [[ "$TEMP_RAW" =~ ^[0-9]+$ ]]; then
        TEMP="${TEMP_RAW}C"
    else
        TEMP="N/A (jq/JSON Fail)"
    fi

    echo "$TEMP"
}

# ===================================================
# 1. CPU 온도 및 사용률 확인 (데스크톱용)
# ===================================================
CPU_TEMP="N/A (lm-sensors)"
if command -v sensors &> /dev/null; then
    # 'sensors' 명령으로 'Package id' 또는 'Core 0' 온도를 찾아 숫자만 추출
    TEMP_LINE=$(sensors | grep -E 'Package id|Core 0' | head -n 1)
    TEMP_RAW=$(echo "$TEMP_LINE" | awk -F'[+°]' '{print $2}' | sed 's/ C$//' | tr -d '[:space:]')
    
    if [[ "$TEMP_RAW" =~ ^[0-9]*\.[0-9]+$ ]] || [[ "$TEMP_RAW" =~ ^[0-9]+$ ]]; then
        CPU_TEMP="${TEMP_RAW}"
    fi
fi

# CPU Usage
CPU_IDLE=$(vmstat 1 2 | tail -n 1 | awk '{print $15}')
CPU_USAGE=$((100 - CPU_IDLE))

# ===================================================
# 2. 디스크 온도 및 I/O 확인
# ===================================================
# A. 디스크 I/O (iostat 사용)
get_disk_io() {
    local DEVICE=$1
    local IOSTAT_DATA=$(iostat -d 1 2 | grep "${DEVICE}" | tail -n 1)
    local DISK_READ_KB=$(echo "$IOSTAT_DATA" | awk '{print $4}')
    local DISK_WRITE_KB=$(echo "$IOSTAT_DATA" | awk '{print $5}')
    
    local READ_MB=$(calculate_io_mb $DISK_READ_KB)
    local WRITE_MB=$(calculate_io_mb $DISK_WRITE_KB)
    
    echo "${READ_MB}|${WRITE_MB}"
}

SSD_TEMP=$(get_disk_temp $DEV_SSD)
HDD_TEMP=$(get_disk_temp $DEV_HDD)

SSD_IO_RAW=$(get_disk_io $DEV_SSD)
SSD_READ_MB=$(echo $SSD_IO_RAW | cut -d'|' -f1)
SSD_WRITE_MB=$(echo $SSD_IO_RAW | cut -d'|' -f2)

HDD_IO_RAW=$(get_disk_io $DEV_HDD)
HDD_READ_MB=$(echo $HDD_IO_RAW | cut -d'|' -f1)
HDD_WRITE_MB=$(echo $HDD_IO_RAW | cut -d'|' -f2)


# ===================================================
# 3. 메모리 사용량 확인 및 계산
# ===================================================
MEM_TOTAL=$(free -m | awk 'NR==2{print $2}')
MEM_AVAILABLE=$(free -m | awk 'NR==2{print $7}')
MEM_REAL_USED=$((MEM_TOTAL - MEM_AVAILABLE))
MEM_PERCENT=$(echo "scale=1; (${MEM_TOTAL} - ${MEM_AVAILABLE}) * 100 / ${MEM_TOTAL}" | bc)


# ===================================================
# 4. 네트워크 트래픽 (MB/s) 확인
# ===================================================
# sar -n DEV 1 2 명령어는 1초 간격으로 2회 측정 후 평균을 냄.
NET_DATA=$(sar -n DEV 1 2 | grep "${NETWORK_INTERFACE}" | tail -n 1)
NET_RX_KB=$(echo "$NET_DATA" | awk '{print $5}')
NET_TX_KB=$(echo "$NET_DATA" | awk '{print $6}')

NET_DOWNLOAD_MB=$(calculate_io_mb $NET_RX_KB)
NET_UPLOAD_MB=$(calculate_io_mb $NET_TX_KB)

# ===================================================
# 5. 결과 출력
# ===================================================
echo "=========================================="
echo "          Desktop PC Monitoring"
echo "=========================================="
echo "CPU Temp: ${CPU_TEMP}C | Usage: ${CPU_USAGE}%"
echo "------------------------------------------"
echo "Disk Temp: SSD: ${SSD_TEMP} | HDD: ${HDD_TEMP}"
echo "------------------------------------------"
echo "Memory Usage: ${MEM_REAL_USED}MB / ${MEM_TOTAL}MB (${MEM_PERCENT}%)"
echo "------------------------------------------"
echo "Network (MB/s): Down: ${NET_DOWNLOAD_MB} | Upload: ${NET_UPLOAD_MB}"
echo "Disk I/O (SSD): Read: ${SSD_READ_MB} | Write : ${SSD_WRITE_MB}"
echo "Disk I/O (HDD): Read: ${HDD_READ_MB} | Write : ${HDD_WRITE_MB}"
echo "=========================================="