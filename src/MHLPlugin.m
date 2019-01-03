//
//  MHLPlugin.m
//
//  Created by anasit on 2019/01/02.
//  Copyright © 2018年 anasit. All rights reserved.
//

#import "MHLPlugin.h"

@implementation MHLPlugin{
    NSMutableArray* mBluetoothDevices;
    NSMutableArray* mPrintJsonArray;
    NSString* mCallbackId;
}

// CordovaPlugin 初始化方法

- (void)pluginInitialize
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handlePrinterConnectedNotification:) name:PrinterConnectedNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handlePrinterDisconnectedNotification:) name:PrinterDisconnectedNotification object:nil];
    [PrinterSDK defaultPrinterSDK];
    [self log:@"初始化SDK"];
}

- (void)devices:(CDVInvokedUrlCommand *)command{
    [self log:@"开始扫描设备"];
    [self performSelector:@selector(scanDevice:) withObject:command afterDelay:1];
}

- (void)scanDevice:(CDVInvokedUrlCommand *)command{
    [[PrinterSDK defaultPrinterSDK] scanPrintersWithCompletion:^(Printer* printer)
     {
         [self log:@"发现新设备"];
         if(mBluetoothDevices == nil){
             mBluetoothDevices = [[NSMutableArray alloc] initWithCapacity:1];
             mPrintJsonArray = [[NSMutableArray alloc] initWithCapacity:1];
         }
         [mBluetoothDevices addObject:printer];
         [mPrintJsonArray addObject:@{@"deviceName":printer.name == nil?@"未知设备":printer.name,@"deviceAddress":printer.UUIDString}];
         CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:mPrintJsonArray];
         [pluginResult setKeepCallbackAsBool:YES];
         [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     }];
}

- (void)connect:(CDVInvokedUrlCommand *)command{
    mCallbackId = command.callbackId;
    NSString* uuid = [command.arguments objectAtIndex:0];
    NSLog(@"MHLPluginLog 连接到设备 uuid:%@", uuid);
    for(NSInteger i = 0; i<mBluetoothDevices.count; i++){
        Printer* printer = [mBluetoothDevices objectAtIndex:i];
        if([printer.UUIDString isEqualToString:uuid]){
            [[PrinterSDK defaultPrinterSDK] connectBT:printer];
            break;
        }
    }
}

-(void)print:(CDVInvokedUrlCommand *)command{
    NSString* printStr = [command.arguments objectAtIndex:0];
    [[PrinterSDK defaultPrinterSDK] printText:printStr];
}

- (void)handlePrinterConnectedNotification:(NSNotification*)notification
{
    [self log:@"接收到设备连接成功通知消息"];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"设备连接成功"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:mCallbackId];
}

- (void)handlePrinterDisconnectedNotification:(NSNotification*)notification
{
    [self log:@"接收到失去设备连接通知消息"];
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"设备连接失败"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:mCallbackId];
}

// 日志打印

- (void)log:(NSString*)message{
    NSLog(@"MHLPluginLog%@", message);
}

@end
