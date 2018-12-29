//
//  SecDecode.h
//  MyApp
//
//  Created by 睢意博 on 2018/7/30.
//

#import <Cordova/CDV.h>
#import "SecDocSDK.framework/Headers/SDSSecDocManager.h"

@interface SecDecode : CDVPlugin

//{
//    NSString * cipherPath;//密文路径
//    NSString * decodePath;//解密后路径
//}

//@property (assign, nonatomic) NSString *cipherPath;
//@property (assign, nonatomic) NSString *decodePath;

//登录
- (void)login:(CDVInvokedUrlCommand *)command;
//解密
- (void)decode:(CDVInvokedUrlCommand *)command;
//是否加密
- (void)isCipher:(CDVInvokedUrlCommand *)command;
//打开文件
- (void)openFile:(CDVInvokedUrlCommand *)command;
@end
