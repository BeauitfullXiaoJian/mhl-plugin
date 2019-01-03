var exec = require('cordova/exec');
var MHLPlugin = {};
MHLPlugin.call = function (callName, params, success, error) {
    exec(success, error, 'MHLPlugin', callName, params);
};
module.exports = MHLPlugin;