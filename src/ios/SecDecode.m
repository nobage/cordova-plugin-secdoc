//
//  SecDecode.m
//  MyApp
//
//  Created by 睢意博 on 2018/7/30.
//

#import "SecDecode.h"
#import "HttpTool.h"
// 服务器地址或IP
#define kBaseUrl @"219.146.4.153"
//msdoc.eetrust.com
//#define kUserName @"15635171086"
#define kAppId @"1"
#define kPort 8088
#define KIsHttps NO

@implementation SecDecode

-(void)login:(CDVInvokedUrlCommand *)command {
    dispatch_async(dispatch_get_main_queue(), ^{
        NSMutableDictionary *args = [NSMutableDictionary dictionaryWithDictionary:[command.arguments objectAtIndex:0]];
        NSDictionary *loginDic = [args valueForKey:@"loginInfo"];
        NSDictionary *configDic = [args valueForKey:@"configInfo"];
        NSString *userName = [loginDic valueForKey:@"userName"];
        NSString *serverIP = [configDic valueForKey:@"serverIP"]? [configDic valueForKey:@"serverIP"] : kBaseUrl;
        NSString *appId = [configDic valueForKey:@"appId"]? [configDic valueForKey:@"appId"] : kAppId;
        int port = [configDic valueForKey:@"port"]?  [[configDic valueForKey:@"port"] intValue]: kPort;
        BOOL isHttps = [configDic valueForKey:@"isHttps"]? [[configDic valueForKey:@"isHttps"] boolValue] : KIsHttps;

        [[SDSSecDocManager sharedInstance] sds_configureWithLoginName:userName serverIP:serverIP port:port isHTTPS:isHttps appId:appId];
        [self pm_getTicketWihtUserName:loginDic andWithCompletionHandler:^(NSString *ticket) {
            [[SDSSecDocManager sharedInstance] sds_authenticateWithTicket:ticket version:@"3.3.1.1" success:^{
                NSLog(@"登录成功，请继续操作");
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self sendResult:CDVCommandStatus_OK andMessageWithBool:true wihtCommand:command];
                });
            } failure:^(NSError *error) {
                [self sendResult:CDVCommandStatus_ERROR andMessageWithBool:false wihtCommand:command];
            }];
        }];
    });
}


- (void)decode:(CDVInvokedUrlCommand *) command {
    dispatch_async(dispatch_get_main_queue(), ^{
        //文件名称
        NSString *cipherFilePath = [command.arguments objectAtIndex:0];
        NSString *cipherPath;
        NSString *fileName;
        if ([cipherFilePath rangeOfString:@"file://"].location != NSNotFound) {
            cipherPath = [cipherFilePath stringByReplacingOccurrencesOfString:@"file://" withString:@""];
            fileName = [[cipherPath componentsSeparatedByString:@"/"] lastObject];
        }
        else{
            fileName = cipherFilePath;
            cipherPath = [NSString stringWithFormat:@"%@/www/assets/file/%@",[[NSBundle mainBundle]resourcePath],cipherFilePath];
        }
        //解密后文件存放路径
        NSString *decodeFilePath = [NSString stringWithFormat:@"%@/p_%@",[self pm_tempPath],fileName];
        [[SDSSecDocManager sharedInstance] sds_decryptWithCipherPath:cipherPath plainPath:decodeFilePath isOpenFile:YES success:^(NSString *rights) {
            //        [self openFile:decodeFilePath];
            [self sendResult:CDVCommandStatus_OK andMessageWithString:@"1" wihtCommand:command];
        } failure:^(NSError *error) {
            NSString *locationStr = [NSString stringWithFormat:@"解密失败：%@",error];
            NSLog(@"%@",locationStr);
            [self sendResult:CDVCommandStatus_ERROR andMessageWithString:locationStr wihtCommand:command];
            //        [[self commandDelegate] evalJs:locationStr];
        }];
    });
}

-(void)isCipher:(CDVInvokedUrlCommand *)command {
    dispatch_async(dispatch_get_main_queue(), ^{
        //文件名称
        NSString *cipherFilePath = [command.arguments objectAtIndex:0];
        NSString *cipherPath;
        if ([cipherFilePath rangeOfString:@"file://"].location != NSNotFound) {
            cipherPath = [cipherFilePath stringByReplacingOccurrencesOfString:@"file://" withString:@""];
        }
        else{
            cipherPath = [NSString stringWithFormat:@"%@/www/assets/file/%@",[[NSBundle mainBundle]resourcePath],cipherFilePath];
        }
        [[SDSSecDocManager sharedInstance] sds_isCiphertextWithDocPath:cipherPath success:^(BOOL flag) {
            [self sendResult:CDVCommandStatus_OK andMessageWithString:[NSString stringWithFormat:@"%d",flag] wihtCommand:command];
        } failure:^(NSError *error) {
            NSString *locationStr = [NSString stringWithFormat:@"判断文件是否加密失败：%@",error];
            NSLog(@"%@",locationStr);
            [self sendResult:CDVCommandStatus_ERROR andMessageWithString:locationStr wihtCommand:command];
        }];
    });
}

-(void)openFile:(CDVInvokedUrlCommand *)command {
  dispatch_async(dispatch_get_main_queue(), ^{
         //文件名称
         NSString *cipherFilePath = [command.arguments objectAtIndex:0];
         NSString *isCipher = [command.arguments objectAtIndex:1];
         NSString *cipherPath;
         NSString *fileName;
         if ([cipherFilePath rangeOfString:@"file://"].location != NSNotFound) {
             cipherPath = [cipherFilePath stringByReplacingOccurrencesOfString:@"file://" withString:@""];
             fileName = [[cipherPath componentsSeparatedByString:@"/"] lastObject];
         }
         else{
             fileName = cipherFilePath;
             cipherPath = [NSString stringWithFormat:@"%@/www/assets/file/%@",[[NSBundle mainBundle]resourcePath],cipherFilePath];
         }
         NSString *decodeFilePath = [NSString stringWithFormat:@"%@/p_%@",[self pm_tempPath],fileName];
         if ([isCipher isEqualToString:@"0"]) {
             decodeFilePath = cipherPath;
         }
         [[SDSSecDocManager sharedInstance] sds_lookDocWithNavigationController:nil docPath:decodeFilePath failure:^(NSError *error) {
             NSString *locationStr = [NSString stringWithFormat:@"打开文件失败：%@", error];
             NSLog(@"%@",locationStr);
             [self sendResult:CDVCommandStatus_ERROR andMessageWithString:locationStr wihtCommand:command];
         }];

     });
}


/*
 向JS返回相应
 */
- (void)sendResult:(CDVCommandStatus)status andMessageWithString:(NSString *)message wihtCommand:(CDVInvokedUrlCommand *)command{
    [self.commandDelegate runInBackground:^{
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:status messageAsString:message];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)sendResult:(CDVCommandStatus)status andMessageWithBool:(BOOL)message wihtCommand:(CDVInvokedUrlCommand *)command{
    [self.commandDelegate runInBackground:^{
        CDVPluginResult *result = [CDVPluginResult resultWithStatus:status messageAsBool:message];
        [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    }];
}

- (void)pm_getTicketWihtUserName:(NSDictionary*)loginInfo andWithCompletionHandler:(void(^)(NSString *ticket))completion
{
    // 登录方法
    NSString *loginPath = @"clientInterface/clientLogin.do";
    NSString *getTicket = @"clientInterface/clientLogin.do?loginType=sdsc_sso";

    [[HttpTool sharedInstance] getWithPath:loginPath params:loginInfo success:^(id responseObject) {
        NSLog(@"str:%@", [self pm_stringWithGBKData:responseObject]);

        [[HttpTool sharedInstance] postWithPath:getTicket params:nil success:^(id responseObject1) {
            NSString *resStr = [self pm_stringWithGBKData:responseObject1];
            NSLog(@"tietck:%@", resStr);

            // 获取ticket
            NSRange startRange = [resStr rangeOfString:@"ticket>"];
            NSRange endRange = [resStr rangeOfString:@"</ticket"];
            NSRange range = NSMakeRange(startRange.location + startRange.length, endRange.location - startRange.location - startRange.length);
            NSString *ticket = [resStr substringWithRange:range];

            completion(ticket);

        } failure:^(NSError *error) {
            NSLog(@"error:%@",error);
        }];

    } failure:^(NSError *error) {
        NSLog(@"error:%@",error);
    }];
}

- (NSString *)pm_tempPath
{
    NSString *ourDocumentPath = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES)[0];
    NSString *path = [NSString stringWithFormat:@"%@/temp",ourDocumentPath];

    [self pm_createFolder:path];
    return path;
}

- (void)pm_createFolder:(NSString *)folderPath
{
    NSFileManager *manager = [NSFileManager defaultManager];
    BOOL existed = [manager fileExistsAtPath:folderPath];
    if (!existed) {
        [manager createDirectoryAtPath:folderPath withIntermediateDirectories:YES attributes:nil error:nil];
    }
}

- (NSString *)pm_stringWithGBKData:(NSData *)data
{
    if (![data isKindOfClass:[NSData class]]) return nil;

    // 声明一个gbk编码类型
    NSStringEncoding gbkEncoding = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    // 使用如下方法 将获取到的数据按照gbkEncoding的方式进行编码，结果将是正常的汉字
    return [[NSString alloc] initWithData:data encoding:gbkEncoding];
}

@end
