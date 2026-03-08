// 配置后端基础地址
// 1. H5 浏览器调试：用电脑局域网 IP (如 192.168.x.x) 或 localhost
// 2. 微信小程序真机：必须用阿里云公网 IP (且要配 HTTPS 或勾选不校验)
// 这里先写死为你的本地 IP 或 localhost，根据你实际运行环境修改
//const BASE_URL = 'http://127.0.0.1:8080';
const BASE_URL = 'https://www.ck0731.com';
// 如果是手机真机调试连电脑，请把 localhost 改成你的电脑局域网 IP，例如 'http://192.168.1.5:8080'

/**
 * 获取存储的token
 */
const getToken = () => {
	return uni.getStorageSync('token') || '';
};

/**
 * 保存token
 */
export const saveToken = (token) => {
	uni.setStorageSync('token', token);
};

/**
 * 清除token
 */
export const clearToken = () => {
	uni.removeStorageSync('token');
};

export const request = (options) => {
	return new Promise((resolve, reject) => {
		const token = getToken();
		const headers = {
			'Content-Type': 'application/json',
			...options.header
		};

		// 如果有token，添加到请求头
		if (token) {
			headers['Authorization'] = 'Bearer ' + token;
		}

		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: headers,
			success: (res) => {
				// 处理401未授权错误
				if (res.statusCode === 401) {
					clearToken();
					uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
					// 跳转到登录页
					uni.reLaunch({ url: '/pages/login/login' });
					reject(new Error('未授权'));
					return;
				}

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

/**
 * 上传单个图片
 * @param {String} filePath 本地文件路径
 * @returns {Promise} 返回上传后的URL
 */
export const uploadImage = (filePath) => {
	return new Promise((resolve, reject) => {
		const token = getToken();
		const header = {};
		if (token) {
			header['Authorization'] = 'Bearer ' + token;
		}

		uni.uploadFile({
			url: BASE_URL + '/upload/image',
			filePath: filePath,
			name: 'file',
			header: header,
			success: (res) => {
				try {
					const data = JSON.parse(res.data);
					if (data.success) {
						resolve(BASE_URL + data.url);
					} else {
						uni.showToast({ title: data.error || '上传失败', icon: 'none' });
						reject(new Error(data.error));
					}
				} catch (e) {
					reject(e);
				}
			},
			fail: (err) => {
				console.error("上传失败:", err);
				uni.showToast({ title: '上传失败', icon: 'none' });
				reject(err);
			}
		});
	});
};

/**
 * 上传多个图片
 * @param {Array} filePaths 本地文件路径数组
 * @returns {Promise} 返回上传后的URL数组
 */
export const uploadImages = async (filePaths) => {
	const urls = [];
	for (const filePath of filePaths) {
		try {
			const url = await uploadImage(filePath);
			urls.push(url);
		} catch (e) {
			console.error('上传图片失败:', e);
		}
	}
	return urls;
};

