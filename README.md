# acsdata
java write acsdata.
通过判断修改acsdata是否成功的方式，确定是否触发升级。
升级前需要将升级包copy到cache，因为正常情况下，recovery只挂在了cache分区。
