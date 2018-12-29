//
//  SDSEncryptParam.h
//  SecureDocSDK
//
//  Created by syn on 2017/6/6.
//  Copyright © 2017年 syn. All rights reserved.
//

#import <Foundation/Foundation.h>

@class SDSUserAndDept;

@interface SDSEncryptParam : NSObject

/** 文档密级 1：普通2：秘密3：机密4：绝密 */
@property (nonatomic, assign) int confidential;
/** 向某些人授权的集合 */
@property (nonatomic, copy) NSArray<SDSUserAndDept *> *users;
/** 向某些机构授权的集合 */
@property (nonatomic, copy) NSArray<SDSUserAndDept *> *depts;
@end
