# 微信登录页源码讲解

目标文件：

- [src/pages/login/index.ts](/Users/lgd/project/forest/apps/trade-leads/clients/client-wechat-miniapp/src/pages/login/index.ts)

这份文档不是重复描述语法，而是按“代码在做什么 + 为什么应该放在页面层”来解释登录页。

## 文件定位

`src/pages/login/index.ts` 是微信小程序登录页的页面编排器。

它负责：

- 接收 `redirect` 路由参数
- 响应用户点击登录
- 调用 app 层登录门面 `loginWithWechat`
- 展示 loading、成功提示、错误提示
- 登录成功后决定跳回哪个页面

它不负责：

- 直接调用 `wx.login`
- 保存 token
- 恢复 `currentUser`
- 处理 401 刷新
- 直接请求 `/api/auth/wechat-miniapp/login`

这些能力已经分别下沉到：

- `apps/.../app-definition.ts`
- `apps/.../miniapp-app.ts`
- `@forest/wechat-miniapp-client-app`
- `@forest/user/wechat-miniapp/auth`

## 逐行注释

### 第 1 行

```ts
import { getWechatLoginErrorMessage, WECHAT_LOGIN_COPY } from '@forest/user/wechat-miniapp/auth'
```

从 `user` 业务模块导入登录文案和错误文案转换函数。

- `WECHAT_LOGIN_COPY` 提供 `loadingText`、`successText`、`failedText`
- `getWechatLoginErrorMessage` 把异常转成可展示给用户的文案

为什么放在 `@forest/user` 是合理的：

- 登录文案属于用户域业务展示模型
- 页面层只负责“显示什么”，不负责定义文案本身

### 第 2 行

```ts
import { miniappAuth, miniappRouter } from '../../miniapp-app'
```

导入 app 层装配好的统一 facade。

页面不会直接：

- 调 `wx.login`
- 调后端登录接口
- 存 token
- 查当前用户

页面只调用：

```ts
await miniappAuth.loginWithWechat()
```

这样登录细节就不会泄漏到页面层。

### 第 3 行

同一个 import 里同时拿到了 `miniappRouter`。

登录成功后的跳转属于客户端路由策略，而不是 `user` 业务规则，所以应该放在 `miniapp-app` 暴露的路由 facade 上。

### 第 5 行

```ts
Page({
```

开始定义一个微信小程序页面。

可以把它类比成 Vue3 里的路由页面组件定义。

### 第 6 到 10 行

```ts
  data: {
    submitting: false,
    errorMessage: '',
    redirect: '/pages/leads/index'
  },
```

这是页面自己的可渲染状态。

- `submitting`：登录按钮是否处于提交中
- `errorMessage`：当前要展示的错误提示
- `redirect`：登录成功后跳回哪个页面

为什么它们在页面层是合理的：

- 这是页面交互状态，不是业务域实体数据
- 它们直接驱动当前页面 UI，而不是跨页面共享状态

### 第 12 行

```ts
  onLoad(options: { redirect?: string }) {
```

页面生命周期。登录页被打开时执行。

这里读取的 `options.redirect` 就是路由参数里的 `redirect`。

### 第 13 到 15 行

```ts
    this.setData({
      redirect: options.redirect ? decodeURIComponent(options.redirect) : '/pages/leads/index'
    })
```

把路由参数里的 `redirect` 写回页面状态。

做了两件事：

1. 有 `redirect` 时先 `decodeURIComponent`
2. 没有时默认回线索首页 `/pages/leads/index`

为什么要解码：

- URL 参数里的特殊字符通常会被编码
- 登录前的目标页面可能本身还带 query，比如详情页参数

### 第 18 行

```ts
  async handleWechatLogin() {
```

定义“点击微信登录按钮”后的处理函数。

这是登录页最核心的方法，但它仍然只做页面编排，不做底层登录实现。

### 第 19 到 21 行

```ts
    if (this.data.submitting) {
      return
    }
```

防止用户重复点击登录按钮。

如果当前已经在登录中，就直接返回，不再发起第二次登录流程。

这是典型的页面层责任，因为它控制的是当前页面的交互节奏。

### 第 23 到 26 行

```ts
    this.setData({
      submitting: true,
      errorMessage: ''
    })
```

登录开始前，先把页面状态切到“提交中”并清空上一次错误。

- `submitting: true` 会让按钮进入 loading 态
- `errorMessage: ''` 避免旧错误残留在界面上

### 第 28 到 30 行

```ts
    wx.showLoading({
      title: WECHAT_LOGIN_COPY.loadingText
    })
```

调用微信平台原生 loading。

这里有一个清晰分层：

- 页面层负责调用 `wx.showLoading`
- `user` 业务模块负责提供 `loadingText`

### 第 32 行

```ts
    let loadingVisible = true
```

定义一个普通局部变量，记录 loading 当前是否还在显示。

为什么它不是 `data`：

- 它不需要渲染到模板
- 它只在这次函数执行期间临时使用
- 它只是控制 `finally` 里是否还需要再 `hideLoading`

### 第 33 行

```ts
    try {
```

开始执行主登录流程。

成功走 `try`，失败走 `catch`，最后统一走 `finally` 收尾。

### 第 34 行

```ts
      // 页面只触发微信登录；openid 解析、账号绑定、JWT 签发都在后端完成。
```

这句注释是当前页面边界的核心说明。

它明确告诉阅读者：

- 页面只触发登录动作
- 身份识别和鉴权不在前端页面层处理

### 第 35 行

```ts
      await miniappAuth.loginWithWechat()
```

真正登录只有这一句。

背后已经封装了完整链路：

- 保证当前 bundle 的 HTTP 已切到 `wx.request`
- 调 `wx.login()` 获取微信 `code`
- 把 `code` 发给后端登录接口
- 保存 token
- 再请求 `currentUser`
- 缓存用户信息

页面这里故意不知道这些细节，这样页面层才够薄。

### 第 36 到 37 行

```ts
      wx.hideLoading()
      loadingVisible = false
```

登录成功后先手动关闭 loading，并把 `loadingVisible` 改成 `false`。

这样 `finally` 里就不会重复 `hideLoading()`。

### 第 38 到 41 行

```ts
      wx.showToast({
        title: WECHAT_LOGIN_COPY.successText,
        icon: 'success'
      })
```

展示登录成功提示。

依然是：

- 页面层负责调微信平台 API
- 业务层负责给文案

### 第 42 行

```ts
      this.openRedirect()
```

登录成功后，按照当前页面保存的 `redirect` 继续打开目标页面。

这里不把跳转逻辑散在 `try` 里，而是收口到 `openRedirect()`，可读性更好。

### 第 43 行

```ts
    } catch (error) {
```

如果登录过程中任意一步抛错，就进入这里。

错误可能来自：

- `wx.login` 失败
- 网络请求失败
- 后端返回业务错误
- 当前用户恢复失败

### 第 44 到 46 行

```ts
      this.setData({
        errorMessage: getWechatLoginErrorMessage(error)
      })
```

把异常转换成用户可读错误信息，再写进页面状态，交给模板渲染。

这里不直接写死错误文案，而是调用 `getWechatLoginErrorMessage(error)`。

好处是：

- 业务错误文案可以统一管理
- 页面层只负责展示，不负责翻译错误

### 第 47 行

```ts
    } finally {
```

无论成功还是失败，最后都执行这里，用于统一收尾。

### 第 48 到 50 行

```ts
      if (loadingVisible) {
        wx.hideLoading()
      }
```

如果前面还没有关闭 loading，这里兜底关闭。

这是为了保证：

- 成功路径不会漏关 loading
- 失败路径也不会漏关 loading

### 第 51 到 53 行

```ts
      this.setData({
        submitting: false
      })
```

无论成功或失败，最终都把按钮恢复到非提交中状态。

这确保用户在失败后还能重新点击登录。

### 第 57 行

```ts
  openRedirect() {
```

定义登录成功后的跳转函数。

它的责任很单一：

- 从页面状态中取目标 URL
- 根据页面类型决定用哪种路由策略打开

### 第 58 行

```ts
    const redirect = this.data.redirect || '/pages/leads/index'
```

读取当前要跳转的页面地址。

如果页面状态里没有有效 `redirect`，就兜底跳到线索首页。

### 第 59 行

```ts
    // 一级入口页用 reLaunch/redirect 语义，详情等普通页面用 replace 保留继续动作。
```

这句注释解释了为什么后面要分支处理不同页面。

不是所有页面都应该用同一种打开方式：

- 一级主页面更适合主入口语义
- 普通详情页更适合替换当前页

### 第 60 到 63 行

```ts
    if (redirect.startsWith('/pages/leads/index') || redirect.startsWith('/pages/unlocked/index') || redirect.startsWith('/pages/me/index')) {
      miniappRouter.openPrimaryPage(redirect)
      return
    }
```

如果目标是一级主页面之一：

- 线索页
- 已解锁页
- 我的页面

就走 `miniappRouter.openPrimaryPage()`。

这属于 app 路由策略，而不是业务域规则，所以放在页面编排层是合理的。

### 第 65 行

```ts
    miniappRouter.replacePage(redirect)
```

如果目标不是一级主页面，就用 `miniappRouter.replacePage()` 打开。

典型场景是：

- 详情页
- 某些流程页
- 带继续动作的普通页面

### 第 67 行

```ts
})
```

页面定义结束。

## 这份文件的架构判断

当前这份登录页实现整体是合理的，因为它只保留了页面层应该保留的东西：

- 页面状态
- 生命周期参数处理
- 微信平台 loading / toast
- 调 app 层登录门面
- 登录成功后的路由跳转

它没有越界去做这些事：

- 获取微信登录 code
- 调后端登录接口
- 管 token 存储
- 管 currentUser 恢复
- 定义登录业务文案

这说明它基本符合当前项目的边界目标：

- `business` 提供业务 API 和业务文案
- `base` / `platform` 提供平台能力
- `app page` 只负责页面编排
