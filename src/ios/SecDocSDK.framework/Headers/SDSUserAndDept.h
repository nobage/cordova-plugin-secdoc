//
//  SDSUserAndDept.h
//  SecureDocSDK
//
//  Created by syn on 2017/6/6.
//  Copyright © 2017年 syn. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SDSUserAndDept : NSObject

/** 用户账号 */
@property (nonatomic, copy) NSString *username;
/** 部门唯一标识 */
@property (nonatomic, copy) NSString *deptId;
/** 授予用户的权限 */
@property (nonatomic, copy) NSString *userRights;
/** 授予机构的权限 */
@property (nonatomic, copy) NSString *deptRights;

@end
