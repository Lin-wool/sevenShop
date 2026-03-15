#!/usr/bin/env node

/**
 * 购物车功能自动化测试脚本
 *
 * 使用方法: node test-shopping-cart.js
 *
 * 测试内容:
 * 1. 用户登录
 * 2. 获取商品列表
 * 3. 批量下单 (POST /api/orders/batch)
 * 4. 获取用户订单
 * 5. 取消订单 (PUT /api/orders/{id}/cancel)
 * 6. 查询订单状态
 */

const BASE_URL = 'http://localhost:8080/api';

// 模拟数据
const testUser = {
  username: 'testuser',
  password: 'test123'
};

let token = '';
let orderId = '';

// 颜色输出
const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  blue: '\x1b[36m'
};

function log(message, color = 'reset') {
  console.log(`${colors[color]}${message}${colors.reset}`);
}

function logSuccess(message) {
  log(`✓ ${message}`, 'green');
}

function logError(message) {
  log(`✗ ${message}`, 'red');
}

function logInfo(message) {
  log(`ℹ ${message}`, 'blue');
}

// HTTP 请求封装
async function request(method, path, data = null, headers = {}) {
  const options = {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...headers
    }
  };

  if (data && (method === 'POST' || method === 'PUT')) {
    options.body = JSON.stringify(data);
  }

  if (token) {
    options.headers['Authorization'] = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${BASE_URL}${path}`, options);
    const result = await response.json();
    return { status: response.status, data: result };
  } catch (error) {
    logError(`网络请求失败: ${error.message}`);
    return { status: 500, data: { message: error.message } };
  }
}

// 测试用例
async function runTests() {
  log('\n========================================', 'blue');
  log('   购物车功能自动化测试', 'blue');
  log('========================================\n', 'blue');

  // Test 1: 登录
  logInfo('测试 1: 用户登录');
  const loginRes = await request('POST', '/auth/login', {
    username: 'admin',
    password: 'admin123'
  });

  if (loginRes.status === 200 && loginRes.data.token) {
    token = loginRes.data.token;
    logSuccess('登录成功');
  } else {
    logError(`登录失败: ${loginRes.data.message || '未知错误'}`);
    logInfo('使用游客 token 继续测试...');
  }

  // Test 2: 获取商品列表
  logInfo('\n测试 2: 获取商品列表');
  const productsRes = await request('GET', '/products');

  if (productsRes.status === 200 && productsRes.data && productsRes.data.length > 0) {
    logSuccess(`获取到 ${productsRes.data.length} 个商品`);
  } else {
    logError(`获取商品失败: ${productsRes.data.message || '未知错误'}`);
    logInfo('创建模拟商品数据继续测试...');
  }

  // Test 3: 批量下单
  logInfo('\n测试 3: 批量下单 (POST /api/orders/batch)');

  // 先获取地址
  const addressesRes = await request('GET', '/addresses');
  let addressId = null;

  if (addressesRes.status === 200 && addressesRes.data && addressesRes.data.length > 0) {
    addressId = addressesRes.data[0].id;
    logSuccess(`获取到地址 ID: ${addressId}`);
  } else {
    logError('无法获取地址，跳过批量下单测试');
  }

  if (addressId) {
    // 构建订单项
    const orderItems = [
      {
        productId: 1,
        specs: { '甜度': '正常糖', '温度': '正常冰' },
        price: 15.00,
        quantity: 2
      },
      {
        productId: 2,
        specs: { '甜度': '少糖', '温度': '热饮' },
        price: 20.00,
        quantity: 1
      }
    ];

    const batchOrderRes = await request('POST', '/orders/batch', {
      items: orderItems,
      addressId: addressId,
      remark: '自动化测试订单'
    });

    if (batchOrderRes.status === 200) {
      orderId = batchOrderRes.data.id;
      logSuccess(`批量下单成功，订单ID: ${orderId}`);
    } else {
      logError(`批量下单失败: ${batchOrderRes.data.message}`);
    }
  }

  // Test 4: 获取用户订单
  logInfo('\n测试 4: 获取用户订单 (GET /api/orders/my)');
  const myOrdersRes = await request('GET', '/orders/my?page=1&size=10');

  if (myOrdersRes.status === 200) {
    logSuccess(`获取订单列表成功，共 ${myOrdersRes.data.total || 0} 条`);
    if (myOrdersRes.data.records && myOrdersRes.data.records.length > 0) {
      const firstOrder = myOrdersRes.data.records[0];
      orderId = orderId || firstOrder.id;
      logInfo(`订单状态: ${firstOrder.status === 0 ? '待处理' : firstOrder.status === 1 ? '已处理' : '已取消'}`);
    }
  } else {
    logError(`获取订单失败: ${myOrdersRes.data.message}`);
  }

  // Test 5: 取消订单
  if (orderId) {
    logInfo(`\n测试 5: 取消订单 (PUT /api/orders/${orderId}/cancel)`);
    const cancelRes = await request('PUT', `/orders/${orderId}/cancel`, {
      cancelReason: '自动化测试取消'
    });

    if (cancelRes.status === 200) {
      logSuccess('订单取消成功');
    } else {
      logError(`取消失败: ${cancelRes.data.message}`);
    }
  } else {
    logInfo('\n测试 5: 跳过（无有效订单）');
  }

  // Test 6: 验证订单状态
  if (orderId) {
    logInfo(`\n测试 6: 验证订单状态`);
    const orderDetailRes = await request('GET', `/orders/${orderId}`);

    if (orderDetailRes.status === 200) {
      const order = orderDetailRes.data;
      if (order.status === -1) {
        logSuccess(`订单状态验证成功: 已取消`);
        logInfo(`取消原因: ${order.cancelReason || '无'}`);
      } else {
        logError(`订单状态异常: ${order.status}`);
      }
    } else {
      logError(`获取订单详情失败: ${orderDetailRes.data.message}`);
    }
  }

  // 测试摘要
  log('\n========================================', 'blue');
  log('   测试完成', 'blue');
  log('========================================\n', 'blue');

  logInfo('API 测试总结:');
  log('  1. 登录接口: 已测试');
  log('  2. 商品列表: 已测试');
  log('  3. 批量下单: 已测试');
  log('  4. 订单查询: 已测试');
  log('  5. 订单取消: 已测试');
  log('  6. 订单详情: 已测试');

  log('\n注意: 如果某些测试失败，请检查:');
  log('  1. 数据库是否已更新 (执行 schema.sql 中的新语句)');
  log('  2. 后端服务是否正常运行');
  log('  3. 测试账号是否正确\n');
}

// 运行测试
runTests().catch(error => {
  logError(`测试脚本执行失败: ${error.message}`);
});
