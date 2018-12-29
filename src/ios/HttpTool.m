//
//  HttpTool.m
//  MyApp
//
//  Created by 睢意博 on 2018/7/30.
//

#import "HttpTool.h"
#import "AFNetworking.h"

#define BasePath @"http://219.146.4.153:8088/securedoc/"
//http://msdoc.eetrust.com/securedoc/
@interface HttpTool ()
@property (nonatomic, strong) AFHTTPSessionManager *sessionManager;
@end

@implementation HttpTool

+ (instancetype)sharedInstance {
    static HttpTool *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}


- (NSURLSessionTask *)getWithPath:(NSString *)path params:(NSDictionary *)params success:(successBlock)success failure:(failBlock)failure
{
    NSString *urlPath = [NSString stringWithFormat:@"%@%@",BasePath,path];
    NSLog(@"get_path:%@", urlPath);

    return [self.sessionManager GET:urlPath parameters:params progress:^(NSProgress * _Nonnull downloadProgress) {
        nil;
    } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        if (success) {
            if (responseObject) success(responseObject);
        }
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        if (failure) {
            if (error) failure(error);
        }
    }];
}

- (NSURLSessionTask *)postWithPath:(NSString *)path params:(NSDictionary *)params success:(successBlock)success failure:(failBlock)failure
{
    NSString *urlPath = [NSString stringWithFormat:@"%@%@",BasePath,path];

    return [self.sessionManager POST:urlPath parameters:params progress:^(NSProgress * _Nonnull uploadProgress) {
        nil;
    } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
        if (success) {
            if (responseObject) success(responseObject);
        }
    } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
        if (failure) {
            if (error) failure(error);
        }
    }];
}

#pragma mark - 懒加载

- (AFHTTPSessionManager *)sessionManager
{
    if (!_sessionManager) {
        _sessionManager = [AFHTTPSessionManager manager];
        //        _sessionManager.requestSerializer = [AFJSONRequestSerializer serializer];
        _sessionManager.responseSerializer = [AFHTTPResponseSerializer serializer];
        _sessionManager.responseSerializer.acceptableContentTypes =  [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"text/html", @"text/plain", @"text/xml", nil];
        _sessionManager.requestSerializer.timeoutInterval = (!self.timeoutInterval ? 15 : self.timeoutInterval);
    }
    return _sessionManager;
}
@end

