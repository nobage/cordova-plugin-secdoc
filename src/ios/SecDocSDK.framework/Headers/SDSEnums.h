//
//  SDSEnums.h
//  SecureDocSDK
//
//  Created by syn on 16/11/9.
//  Copyright © 2016年 syn. All rights reserved.
//

#ifndef SDSEnums_h
#define SDSEnums_h

///-----------
/// 错误类型
///-----------
typedef NS_ENUM(NSInteger, SDSErrorType) {
    /** 身份认证失败 */
    SDSErrorType_AuthenticationFailure = 0x00000001,
    
    /** 解密失败 */
    SDSErrorType_DecryptFailure = 0x00000002,
    /** 加密失败 */
    SDSErrorType_EncryptFailure = 0x00000003,
    /** 不是密文 */
    SDSErrorType_NotCiphertext = 0x00000004,
    /** 已是密文 */
    SDSErrorType_IsCiphertext = 0x00000005,
    
    /** 没有权限 */
    SDSErrorType_NoPermission = 0x00000006,
    /** 文件不存在 */
    SDSErrorType_NotExistFile = 0x00000007,
    /** 权限获取失败 */
    SDSErrorType_QueryRightsFailure = 0x00000008,
    /** 参数错误 */
    SDSErrorType_ParamsError =	0x00000009,
    /** 密文和明文路径相同 */
    SDSErrorType_PathError = 0x0000000A,
    
    /** 网络连接失败 */
    SDSErrorType_ConnectionFailure	= 0x0000000B,
    /** 网络请求超时 */
    SDSErrorType_NetReqTimeout = 0x0000000C,
    /** 网络请求失败 */
    SDSErrorType_NetReqFailure = 0x0000000D,
    
    /** 不支持的文件格式 */
    SDSErrorType_TypeError = 0x0000000E,
};

// 日志：操作类型
typedef  NS_ENUM(NSInteger, SDSOperationType) {
    /** 阅读 */
    SDSOperationTypeRead = 1,
    /** 解密 */
    SDSOperationTypeDecrypt = 7,
};

#endif /* SDSEnums_h */
