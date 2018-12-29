var exec = require('cordova/exec');

exports.login = function (arg0, success, error) {
    exec(success, error, 'SecDecode','login', [arg0]);
};
exports.isCipher = function (arg0, success, error) {
    exec(success, error, 'SecDecode', 'isCipher',[arg0]);
}
exports.decode = function (arg0, success, error) {
    exec(success, error, 'SecDecode', 'decode',[arg0]);
}
exports.openFile = function (arg0, arg1, success, error) {
    exec(success, error, 'SecDecode', 'openFile',[arg0, arg1]);
}