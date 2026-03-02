// 配置后端基础地址
// 1. H5 浏览器调试：用电脑局域网 IP (如 192.168.x.x) 或 localhost
// 2. 微信小程序真机：必须用阿里云公网 IP (且要配 HTTPS 或勾选不校验)
// 这里先写死为你的本地 IP 或 localhost，根据你实际运行环境修改
//const BASE_URL = 'http://127.0.0.1:8080'; 
const BASE_URL = 'https://www.ck0731.com'; 
// 如果是手机真机调试连电脑，请把 localhost 改成你的电脑局域网 IP，例如 'http://192.168.1.5:8080'

export const request = (options) => {
	return new Promise((resolve, reject) => {
		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: {
				'Content-Type': 'application/json'
			},
			success: (res) => {
				// 后端直接返回 List 的情况
				resolve(res.data); 
			},
			fail: (err) => {
				console.error("请求失败:", err);
				uni.showToast({ title: '网络错误', icon: 'none' });
				reject(err);
			}
		});
	});
};