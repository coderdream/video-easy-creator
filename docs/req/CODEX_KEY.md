密钥：cr_5f730e8a9831c932153764a2bd1b1f5bb1b77fc07f960e358a08688a7ba355fd
API请求地址：https://gmn.chuangzuoli.cn/openai

三、注意事项：
如果还有小伙伴们连不上的,或者报错401之类的，可以检查下CODEX的安装目录，一般是安装在当前用户的.codex目录下找到config.toml文件，按照以下格式填入：
model_provider = "custom"
model = "gpt-5.2"
model_reasoning_effort = "xhigh"
disable_response_storage = true

[model_providers.custom]
name = "custom"
wire_api = "responses"
requires_openai_auth = true
base_url = "https://gmn.chuangzuoli.cn/openai"

将上面这段复制进去即可！这是全局API配置。

然后打开命令行输入:
codex --dangerously-bypass-approvals-and-sandbox
启动CODEX就行了


配置 Codex 环境变量
如果你使用支持 OpenAI API 的工具（如 Codex），需要设置以下环境变量：

Codex 配置文件
在 ~/.codex/config.toml 文件开头添加以下配置：

model_provider = "crs"
model = "gpt-5-codex"
model_reasoning_effort = "high"
disable_response_storage = true
preferred_auth_method = "apikey"
[model_providers.crs]
name = "crs"
base_url = "https://gmn.chuangzuoli.cn/openai"
wire_api = "responses"
requires_openai_auth = true
env_key = "CRS_OAI_KEY"
在 ~/.codex/auth.json 文件中配置API密钥：

{
"OPENAI_API_KEY": null
}
💡 将 OPENAI_API_KEY 设置为 null，然后设置环境变量 CRS_OAI_KEY 为您的 API 密钥（格式如 cr_xxxxxxxxxx）。

环境变量设置方法
Windows:

set CRS_OAI_KEY=cr_xxxxxxxxxx
