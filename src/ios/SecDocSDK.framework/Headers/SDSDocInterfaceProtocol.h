//
//  SDSDocInterfaceProtocol.h
//  SecureDocSDKDemo
//
//  Created by syn on 2017/5/12.
//  Copyright © 2017年 syn. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol SDSDocInterfaceProtocol <NSObject>

@required
/**
 查询权限

 @param params 参数
 @param success 成功回调
 @param failure 失败回调
 */
- (void)queryRightsWithParams:(NSDictionary *)params success:(void(^)(id response))success failure:(void(^)(NSError *error))failure;

/**
 发布
 
 @param params 参数
 @param success 成功回调
 @param failure 失败回调
 */
- (void)issueDocWithParams:(NSDictionary *)params success:(void(^)(id response))success failure:(void(^)(NSError *error))failure;

/** 
 授权
 
 @param params 参数
 @param success 成功回调
 @param failure 失败回调
 */
- (void)grantRightsWithParams:(NSDictionary *)params success:(void(^)(id response))success failure:(void(^)(NSError *error))failure;

/**
 发送日志

 @param params 参数
 @param success 成功回调
 @param failure 失败回调
 */
- (void)sendLogWithParams:(NSDictionary *)params success:(void(^)(id response))success failure:(void(^)(NSError *error))failure;

/**
 获取作者默认权限

 @param params 参数
 @param success 成功回调
 @param failure 失败回调
 */
- (void)queryPolicyRightsWithParams:(NSDictionary *)params success:(void(^)(id response))success failure:(void(^)(NSError *error))failure;

@end
