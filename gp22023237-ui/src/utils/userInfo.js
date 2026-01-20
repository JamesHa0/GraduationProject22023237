import useUserStore from '@/store/modules/user';

/**
 * 获取用户信息
 * @param {string} property - 要获取的属性名
 * @returns {*} 属性值或 undefined
 */
export const getUserInfo = (property) => {
    const userStore = useUserStore();
    return userStore.roleInfo?.[0]?.[property];
};

/**
 * 更新用户数据中的指定属性
 * @param {string} property - 要更新的属性名
 * @param {*} value - 要更新的属性值
 * @param {number} [index=0] - 角色信息数组的索引，默认为0
 * @returns {Promise<void>} 更新操作的Promise
 */
export const updateUserData = async (property, value, index = 0) => {
    try {
        const userStore = useUserStore();

        // 检查并初始化 roleInfo 数组
        if (!userStore.roleInfo || !Array.isArray(userStore.roleInfo)) {
            userStore.roleInfo = [];
        }

        // 确保指定索引处有数据
        if (!userStore.roleInfo[index]) {
            userStore.roleInfo[index] = {};
        }

        // 更新指定属性
        userStore.roleInfo[index][property] = value;

        console.log(`更新后用户数据中${property}值为`, userStore.roleInfo[index][property]);

        return Promise.resolve();
    } catch (error) {
        console.error('更新用户数据失败:', error);
        return Promise.reject(error);
    }
};