//
//  HttpTool.h
//  MyApp
//
//  Created by 睢意博 on 2018/7/30.
//

//  发送请求的工具类

#import <Foundation/Foundation.h>

/**
 *  请求成功block
 */
typedef void (^successBlock)(id responseObject);

/**
 *  请求失败block
 */
typedef void (^failBlock)(NSError *error);

@interface HttpTool : NSObject

/** 请求超时时间 */
@property (nonatomic, assign) NSTimeInterval timeoutInterval;

/**
 *  构造单例
 */
+ (instancetype)sharedInstance;

/**
 *  GET请求方法
 *
 *  @param path     路径
 *  @param params   参数
 *  @param success  成功返回的block
 *  @param failure  失败返回的block
 *
 *  @return NSURLSessionTask
 */
- (NSURLSessionTask *)getWithPath:(NSString *)path
                           params:(NSDictionary *)params
                          success:(successBlock) success
                          failure:(failBlock)failure;

/**
 *  POST请求方法
 *
 *  @param path     路径
 *  @param params   参数
 *  @param success  成功返回的block
 *  @param failure  失败返回的block
 *
 *  @return NSURLSessionTask
 */
- (NSURLSessionTask *)postWithPath:(NSString *)path
                            params:(NSDictionary *)params
                           success:(successBlock) success
                           failure:(failBlock)failure;
@end

