FROM ubuntu:20.04

COPY fonts/* /usr/share/fonts/chienes/

RUN echo "deb http://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse\ndeb-src http://mirrors.aliyun.com/ubuntu/ focal main restricted universe multiverse\ndeb http://mirrors.aliyun.com/ubuntu/ focal-security main restricted universe multiverse\ndeb-src http://mirrors.aliyun.com/ubuntu/ focal-security main restricted universe multiverse\ndeb http://mirrors.aliyun.com/ubuntu/ focal-updates main restricted universe multiverse\ndeb-src http://mirrors.aliyun.com/ubuntu/ focal-updates main restricted universe multiverse\ndeb http://mirrors.aliyun.com/ubuntu/ focal-proposed main restricted universe multiverse\ndeb-src http://mirrors.aliyun.com/ubuntu/ focal-proposed main restricted universe multiverse\ndeb http://mirrors.aliyun.com/ubuntu/ focal-backports main restricted universe multiverse\ndeb-src http://mirrors.aliyun.com/ubuntu/ focal-backports main restricted universe multiverse" > /etc/apt/sources.list &&\
	apt-get clean && apt-get update &&\
	apt-get install -y locales && apt-get install -y language-pack-zh-hans &&\
	localedef -i zh_CN -c -f UTF-8 -A /usr/share/locale/locale.alias zh_CN.UTF-8 && locale-gen zh_CN.UTF-8 &&\
	apt-get install -y tzdata && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime &&\
	apt-get install -y libxrender1 && apt-get install -y libxt6 && apt-get install -y libxext-dev && apt-get install -y libfreetype6-dev &&\
	apt-get install -y wget && apt-get install -y ttf-mscorefonts-installer && apt-get install -y fontconfig &&\
	apt-get install ttf-wqy-microhei &&\
	apt-get install ttf-wqy-zenhei &&\
	apt-get install xfonts-wqy &&\
    cd /tmp &&\
	wget https://kkfileview.keking.cn/server-jre-8u251-linux-x64.tar.gz &&\
	tar -zxf /tmp/server-jre-8u251-linux-x64.tar.gz && mv /tmp/jdk1.8.0_251 /usr/local/ &&\

#	安装 OpenOffice
	wget https://kkfileview.keking.cn/Apache_OpenOffice_4.1.6_Linux_x86-64_install-deb_zh-CN.tar.gz -cO openoffice_deb.tar.gz &&\
	tar -zxf /tmp/openoffice_deb.tar.gz && cd /tmp/zh-CN/DEBS &&\
	dpkg -i *.deb && dpkg -i desktop-integration/openoffice4.1-debian-menus_4.1.6-9790_all.deb &&\

#	安装 libreoffice
#    apt-get install -y libxinerama1 libcairo2 libcups2 libx11-xcb1 &&\
#    wget https://mirrors.cloud.tencent.com/libreoffice/libreoffice/stable/7.1.3/deb/x86_64/LibreOffice_7.1.3_Linux_x86-64_deb.tar.gz -cO libreoffice_deb.tar.gz &&\
#    tar -zxf /tmp/libreoffice_deb.tar.gz && cd /tmp/LibreOffice_7.1.3.2_Linux_x86-64_deb/DEBS &&\
#    dpkg -i *.deb &&\

	rm -rf /tmp/* && rm -rf /var/lib/apt/lists/* &&\
    cd /usr/share/fonts/chienes &&\
    mkfontscale &&\
    mkfontdir &&\
    fc-cache -fv
ENV JAVA_HOME /usr/local/jdk1.8.0_251
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV PATH $PATH:$JAVA_HOME/bin
ENV LANG zh_CN.UTF-8
ENV LC_ALL zh_CN.UTF-8

ADD ./server/target/kkFileView-*.tar.gz /opt/
ENV KKFILEVIEW_BIN_FOLDER /opt/kkFileView-3.5.1/bin
ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider","-Dspring.config.location=/opt/kkFileView-3.5.1/config/application.properties","-jar","/opt/kkFileView-3.5.1/bin/kkFileView-3.5.1.jar"]
