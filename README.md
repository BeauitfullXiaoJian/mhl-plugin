# MHL-PLUGIN QSPrinter Cordova Plugin

## 注意
1. 此插件专为指定应用编写的，不可直接用于项目（如果你也是对接此打印机的话，那么可以直接使用），仅用于参考。
2. 由于Cordova版本更新较快，此插件可能无法安装

## 安装

1. `cordova plugin add mhl-plugin`
2. 需要手动设置**Library Search Path**值为"项目目录/Plugins/mhl-plugin"，这个参考**Header Search Paths**的值即可

## 使用
```typescript
// 获取所有可用的蓝牙设备
window.MHLPlugin && window.MHLPlugin.call('devices', null, (devices) => {
    alert(JSON.stringify(devices));
    // 连接打印机需要使用 devices[0].deviceAddress
});

// 连接到指定打印机
 window.MHLPlugin.call('connect', ['deviceAddress'], (msg) => {
    alert(msg);
});

// 打印文字
window.MHLPlugin.call('print', [`
客户名称： 张三
订单日期： 2019
商品名称 数量 单价 金额
方便面   2   2.5 5.0
棒棒糖   2   2.5 5.0
可口可乐 1   2.5 5.0
合计： 100
公司名称：测试公司名称
送货电话: 123456789`]);

// 打印图片
window.MHLPlugin.call('print', [`AVDFejwqilrjfvdfsaASDEr----base64串----rewqr12xczvg`]);
```
