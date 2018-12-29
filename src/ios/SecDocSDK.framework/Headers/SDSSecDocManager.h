//
//  SDSSecureDocManager.h
//  Pods
//
//  Created by syn on 16/11/3.
//
//

#import <UIKit/UIKit.h>
#import "SDSDocInterfaceProtocol.h"
#import "SDSEnums.h"
#import "SDSEncryptParam.h"
#import "SDSUserAndDept.h"

NS_ASSUME_NONNULL_BEGIN

@interface SDSSecDocManager : NSObject

@property (nonatomic, strong, readonly) id<SDSDocInterfaceProtocol> interface;
    
/**
 配置方法

 @param loginName 登录名
 @param interface 接口实例
 @param appId 服务器分配该参数
 @param isOffLine 是否支持离线策略
 @param error 报错信息
 @return YES:成功 NO:失败
 */
- (BOOL)sds_configWithLoginName:(NSString *)loginName interface:(id<SDSDocInterfaceProtocol>)interface appId:(NSString *)appId isOffLine:(BOOL)isOffLine error:(NSError **)error;

/**
 配置方法

 @param loginName 登录名
 @param serverIP 服务器IP地址
 @param port 端口号
 @param isHTTPS 是否是HTTPS请求
 @param appId 服务器分配该参数
 @return YES:成功 NO:失败
 */
- (BOOL)sds_configureWithLoginName:(nullable NSString *)loginName serverIP:(NSString *)serverIP port:(int)port isHTTPS:(BOOL)isHTTPS appId:(nullable NSString *)appId;

/**
 *  SDK连接文档安全服务器
 *
 *  @param ticket     票据
 *  @param success    成功回调
 *  @param failure    失败回调
 */
- (void)sds_authenticateWithTicket:(NSString *)ticket success:(void(^)())success failure:(void(^)(NSError *error))failure;

/**
 SDK连接文档安全服务器

 @param ticket 票据
 @param version 版本号
 @param success 成功回调
 @param failure 失败回调
 */
- (void)sds_authenticateWithTicket:(NSString *)ticket version:(nullable NSString *)version success:(void(^)())success failure:(void(^)(NSError *error))failure;

/**
 *  是否为加密文档
 *
 *  @param docPath  文件路径
 *  @param success  成功回调 YES：是加密文件 NO：不是加密文件
 *  @param failure  失败回调
 */
- (void)sds_isCiphertextWithDocPath:(NSString *)docPath success:(void(^)(BOOL flag))success failure:(void(^)(NSError *error))failure;

/**
 解密文档

 @param cipherPath 密文路径
 @param plainPath 解密后的明文路径
 @param isOpenFile 是否只用于打开文档
 @param success 成功回调 rights：文件权限
 @param failure 失败回调
 */
- (void)sds_decryptWithCipherPath:(NSString *)cipherPath
                        plainPath:(NSString *)plainPath
                       isOpenFile:(BOOL)isOpenFile
                          success:(void(^)(NSString *rights))success
                          failure:(void(^)(NSError *error))failure;

/**
 加密文件
 
 @param plainPath 明文路径
 @param cipherPath 密文路径
 @param success 成功回调
 @param failure 失败回调
 */
- (void)sds_encryptWithPlainPath:(NSString *)plainPath cipherPath:(NSString *)cipherPath encryptParam:(SDSEncryptParam *)encryptParam success:(void(^)())success failure:(void(^)(NSError *error))failure;

/**
 查询权限接口

 @param cipherPath 密文路径
 @param success 成功回调。 rights权限字段
 @param failure 失败回调
 */
- (void)sds_queryRightsWithCipherPath:(NSString *)cipherPath success:(void(^)(NSString *rights))success failure:(void(^)(NSError *error))failure;

/**
 授权接口

 @param cipherPath 密文路径
 @param users 授权给用户的数组
 @param depts 授权给部门的数组
 @param success 成功回调
 @param failure 失败回调
 */
- (void)sds_grantRightsWithCipherPath:(NSString *)cipherPath users:(NSArray<SDSUserAndDept *> *)users depts:(NSArray<SDSUserAndDept *> *)depts success:(void(^)())success failure:(void(^)(NSError *error))failure;

/**
 日志接口
 
 @param cipherPath 被操作的密文文档
 @param operType 操作类型
 @param operResult 操作结果。 YES：操作成功 NO：操作失败
 @param errorInfo 操作失败的原因。操作成功时可为nil，失败时不可为nil。
 */
- (void)sds_sendLogWithCipherPath:(NSString *)cipherPath operType:(SDSOperationType)operType operResult:(BOOL)operResult errorInfo:(NSString *)errorInfo;


/**
 内嵌打开文档（带水印）

 @param navigationController 如果需要push到文档界面就传，不需要不传
 @param docPath 文件路径
 @param failure 失败回调
 */
- (void)sds_lookDocWithNavigationController:(nullable UINavigationController *)navigationController docPath:(NSString *)docPath failure:(void(^)(NSError *error))failure;

#pragma mark - 与认证SDK结合

/**
 认证身份

 @param ticket 认证身份所需票据
 @param success 成功回调
 @param failure 失败回调
 */
- (void)sds_authTicket:(NSString *)ticket success:(void(^)(NSString *loginName))success failure:(void(^)(NSError *error))failure;

#pragma mark - other

/**
 获取SDK版本号

 @return 版本号
 */
- (NSString *)sds_getVersion;

/**
 构造单例
 */
+ (instancetype)sharedInstance;

@end

NS_ASSUME_NONNULL_END
