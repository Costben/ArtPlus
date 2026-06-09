#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
启动带认证的HTTP文件服务器
共享项目根目录到指定端口
"""

import sys
import os
import base64
from http.server import HTTPServer, SimpleHTTPRequestHandler
from pathlib import Path

# 添加src目录到路径
script_dir = Path(__file__).parent.absolute()
project_root = script_dir.parent.absolute()

# 配置
PORT = 5244
USERNAME = 'admin'
PASSWORD = 'admin123'
SHARED_DIRECTORY = project_root


class AuthHTTPRequestHandler(SimpleHTTPRequestHandler):
    """带基本认证的HTTP请求处理器"""
    
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(SHARED_DIRECTORY), **kwargs)
    
    def do_HEAD(self):
        """处理HEAD请求"""
        if self.authenticate():
            super().do_HEAD()
    
    def do_GET(self):
        """处理GET请求"""
        if self.authenticate():
            super().do_GET()
    
    def do_POST(self):
        """处理POST请求"""
        if self.authenticate():
            super().do_POST()
    
    def authenticate(self):
        """验证用户身份"""
        auth_header = self.headers.get('Authorization')
        
        if auth_header is None:
            self.send_auth_required()
            return False
        
        # 解析Basic认证
        try:
            auth_type, auth_string = auth_header.split(' ', 1)
            if auth_type.lower() != 'basic':
                self.send_auth_required()
                return False
            
            decoded = base64.b64decode(auth_string).decode('utf-8')
            username, password = decoded.split(':', 1)
            
            if username == USERNAME and password == PASSWORD:
                return True
            else:
                self.send_auth_required()
                return False
        except Exception:
            self.send_auth_required()
            return False
    
    def send_auth_required(self):
        """发送401认证要求"""
        self.send_response(401)
        self.send_header('WWW-Authenticate', 'Basic realm="ArtPlus File Server"')
        self.send_header('Content-type', 'text/html')
        self.end_headers()
        self.wfile.write(b'<html><body><h1>401 Unauthorized</h1><p>Authentication required.</p></body></html>')
    
    def log_message(self, format, *args):
        """自定义日志格式"""
        print(f"[{self.address_string()}] {format % args}")


def main():
    """主函数"""
    server_address = ('', PORT)
    httpd = HTTPServer(server_address, AuthHTTPRequestHandler)
    
    print("="*60)
    print("ArtPlus 文件共享服务器")
    print("="*60)
    print(f"共享目录: {SHARED_DIRECTORY}")
    print(f"端口: {PORT}")
    print(f"用户名: {USERNAME}")
    print(f"密码: {'*' * len(PASSWORD)}")
    print()
    print(f"服务器地址: http://localhost:{PORT}/")
    print(f"           http://0.0.0.0:{PORT}/")
    print()
    print("按 Ctrl+C 停止服务器")
    print("="*60)
    print()
    
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        print("\n正在停止服务器...")
        httpd.shutdown()
        print("服务器已停止")


if __name__ == "__main__":
    main()

