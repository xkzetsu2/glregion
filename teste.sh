SID=$(( $(date +%s) - 180 ))
q() { curl -s "http://vas.samsungapps.com/product/getContentCategoryProductList.as?contentCategoryID=0000005309&deviceId=$1&sdkVer=36&mcc=$2&mnc=$3&csc=$4&imgWidth=512&imgHeight=512&startNum=1&endNum=100&alignOrder=alphabetical&callerId=com.samsung.android.goodlock&cc=NONE&systemId=$SID&abiType=64&oneUiVersion=80000"; }
M=$(adb shell getprop ro.product.model | tr -d '\r')
echo "só o modelo S25 Ultra:"; q SM-S938B 724 05 XAA | grep -c '<appInfo>'
echo "outro A-series (A56):";  q SM-A566B 724 05 XAA | grep -c '<appInfo>'
echo "os que você recebe hoje ($M):"
q $M 724 05 XAA | grep -oP '(?<=<productName><!\[CDATA\[)[^\]]+'
