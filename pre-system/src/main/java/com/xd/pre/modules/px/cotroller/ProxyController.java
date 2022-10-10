package com.xd.pre.modules.px.cotroller;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

@RestController
@RequestMapping("jd")
public class ProxyController {

    private String targetAddr = "https://wx.tenpay.com";

    /**
     * 代理所有请求
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/proxy/**", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws IOException, URISyntaxException {
        // String url = URLDecoder.decode(request.getRequestURL().toString(), "UTF-8");
        URI uri = new URI(request.getRequestURI());
        String path = uri.getPath();
        String query = request.getQueryString();
        String target = targetAddr + path.replace("/jd/proxy", "");
        if (query != null && !query.equals("") && !query.equals("null")) {
            target = target + "?" + query;
        }
        URI newUri = new URI(target);
        // 执行代理查询
        String methodName = request.getMethod();
        HttpMethod httpMethod = HttpMethod.resolve(methodName);
        if (httpMethod == null) {
            return;
        }
        ClientHttpRequest delegate = new SimpleClientHttpRequestFactory().createRequest(newUri, httpMethod);
        Enumeration<String> headerNames = request.getHeaderNames();
//        // 设置请求头
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            Enumeration<String> v = request.getHeaders(headerName);
//            List<String> arr = new ArrayList<>();
//            while (v.hasMoreElements()) {
//                arr.add(v.nextElement());
//            }
//            delegate.getHeaders().addAll(headerName, arr);
//        }
        //            .addHeader("Referer", "https://pay.m.jd.com/")
        delegate.getHeaders().add("Referer", "https://pay.m.jd.com/");
        StreamUtils.copy(request.getInputStream(), delegate.getBody());
        // 执行远程调用
        ClientHttpResponse clientHttpResponse = delegate.execute();
        response.setStatus(clientHttpResponse.getStatusCode().value());
        // 设置响应头
        clientHttpResponse.getHeaders().forEach((key, value) -> value.forEach(it -> {
            response.setHeader(key, it);
        }));
        StreamUtils.copy(clientHttpResponse.getBody(), response.getOutputStream());
    }

}