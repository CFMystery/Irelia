/**
 *    Copyright 2018 chengfan(fanhub.cn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.fanhub.irelia.server.handler;

import cn.fanhub.irelia.common.utils.ResponseUtil;
import cn.fanhub.irelia.common.utils.SignUtil;
import cn.fanhub.irelia.core.handler.AbstractPreHandler;
import cn.fanhub.irelia.core.model.IreliaRequest;
import cn.fanhub.irelia.core.model.IreliaResponse;
import cn.fanhub.irelia.core.model.IreliaResponseCode;
import cn.fanhub.irelia.core.model.RpcConfig;
import cn.fanhub.irelia.server.http.HeaderKey;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import sun.security.rsa.RSAPublicKeyImpl;

/**
 *
 * @author chengfan
 * @version $Id: SecurityHandler.java, v 0.1 2018年04月09日 下午10:48 chengfan Exp $
 */
@Slf4j
@Sharable
public class SecurityHandler extends AbstractPreHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        IreliaRequest ireliaRequest = (IreliaRequest)msg;
        RpcConfig rpcConfig = ireliaRequest.getRpcConfig();
        if (!rpcConfig.isOpen()) {
            IreliaResponse response = new IreliaResponse();
            response.setCode(IreliaResponseCode.NOT_OPEN_RPC.getCode());
            response.setMessage(IreliaResponseCode.NOT_OPEN_RPC.getMessage());
            ResponseUtil.send(ctx, response, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        if (rpcConfig.isNeedSign()) {
            // 签名校验
            String transSign = ireliaRequest.getHeaders().get(HeaderKey.sign.name());
            String publicKey = ireliaRequest.getSystemConfig().getPublicKey();
            String body = ireliaRequest.getRequestArgs().toJSONString();
            if (transSign == null || publicKey == null ) {
                IreliaResponse response = new IreliaResponse();
                response.setCode(IreliaResponseCode.NO_SIGN_KEY_OR_VALUE.getCode());
                response.setMessage(IreliaResponseCode.NO_SIGN_KEY_OR_VALUE.getMessage());
                ResponseUtil.send(ctx, response, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            boolean verifySign = SignUtil.verifySign(new RSAPublicKeyImpl(publicKey.getBytes()), body, transSign.getBytes());
            if (!verifySign) {
                IreliaResponse response = new IreliaResponse();
                response.setCode(IreliaResponseCode.SIGN_ERROR.getCode());
                response.setMessage(IreliaResponseCode.SIGN_ERROR.getMessage());
                ResponseUtil.send(ctx, response, HttpResponseStatus.BAD_REQUEST);
                return;
            }
        }
        ctx.fireChannelRead(ireliaRequest);
    }

    public int order() {
        return 20;
    }
}