const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext();
  const page = await context.newPage();

  // 收集控制台错误
  const errors = [];
  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push(msg.text());
    }
  });

  page.on('pageerror', error => {
    errors.push(`Page Error: ${error.message}`);
  });

  try {
    console.log('正在打开 http://localhost:5173 ...');
    await page.goto('http://localhost:5173', { waitUntil: 'networkidle', timeout: 30000 });

    // 等待一下让 Vue 渲染
    await page.waitForTimeout(2000);

    // 获取页面标题和内容
    const title = await page.title();
    console.log('页面标题:', title);

    // 获取 body 内容
    const bodyHTML = await page.evaluate(() => document.body.innerHTML);
    console.log('Body 内容:', bodyHTML.substring(0, 500));

    if (errors.length > 0) {
      console.log('\n=== 控制台错误 ===');
      errors.forEach(e => console.log(e));
    } else {
      console.log('\n没有控制台错误');
    }

  } catch (error) {
    console.error('测试失败:', error.message);
  } finally {
    await browser.close();
  }
})();