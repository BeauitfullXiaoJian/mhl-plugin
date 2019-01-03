//
//  MHLPlugin.h
//
//  Created by anasit on 2019/01/02.
//  Copyright © 2018年 anasit. All rights reserved.
//

#import <Cordova/CDVPlugin.h>
#import "PrinterSDK.h"

@interface MHLPlugin : CDVPlugin
{
}

- (void)devices:(CDVInvokedUrlCommand*)command;
- (void)connect:(CDVInvokedUrlCommand*)command;

@end
