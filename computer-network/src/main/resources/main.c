struct sockaddr_un {
    // 固定为AF_LOCAL
    unsigned short sun_family;
    // 路径名
    char sun_path[108];
};