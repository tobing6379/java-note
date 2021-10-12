# Java IO

## Java IO分类

Java IO可以从「传输方式」和「数据操作」两方面进行分类。

### 传输方式

从「数据传输方式」，可以将IO类分为：

+ **字节流**：适合计算机储存，每次读写单个字节，主要用于储存二进制文件，如图片、音频、视频等；
+ **字符流**：适合人类的查看，每次读写单个字符，根据不同字符集，每个字符展开的字节数不同，主要用于储存文本文件。

![image-20210930103131751](https://gitee.com/tobing/imagebed/raw/master/image-20210930103131751.png)

### 相互转换

可以通过**编码**将字符转换为字节，可以通过**解码**将字节重新组合成字符。

编码和解码时可以使用不同的编码方式，常见的编码方式主要有：

+ GBK：中文字符占2个字节，英文字符占1个字节；
+ UTF-8：中文字符占3个字节，英文字符占1个字节；
+ UTF-16be：中文字符和英文字符都是用2个字节（be表示Big Endian，大端）；

Java 的 char 类型使用双字节编码的 UTF-16be 编码。char 类型只占 16 位，即两个字节，可以使得一个中文和一个英文都可以使用一个 char 来储存。

### 数据操作

从数据来源或操作对象，可以将IO分为：

+ **文件操作流**：FileInputStream、FileOutputStream、FileReader、FileWriter；
+ **数组操作流**：ByteArrayInputStream、ByteArrayOutputStream、CharArrayReader、CharArrayWriter；
+ **管道操作流**：PipedInputStream、PipedOutputStream、PipedWrite、PipedReader；
+ **基本数据类型**：DataInputStream、DataOutputStream；
+ **缓冲流**：BufferedInputStream、BufferedOutputStream、BufferedReader、BufferedWriter；
+ **打印流**：PrintStream、PrintWriter；
+ **对象序列化流**：ObjectInputStream、ObjectOutputStream；
+ **转换**：InputStreamReader、OutputStreamWrite；

注意：输入输出方向是从内存的角度而言，内存==>磁盘(输出)、内存<==磁盘(输入)

## 装饰器模式

Java IO框架基本基于装饰器模式来实现。

### 装饰器模式

在装饰器模式中，装饰这和具体组件都继承自组件，具体组件的方法实现不需要依赖其他对象，而装饰这组合一个组件，可以装饰其他装饰这或具体组件。

装饰，就把装饰者套在被装饰者之上，从而动态扩展被装饰者的功能。装饰者的方法有一部分是自己的，属于它自己的功能，然后调用被装饰者的方法实现，从而保留被装饰者的功能。可以看到，具体组件应该是装饰层次的最低层，因为只有具体组件的方法不需要依赖其他对象。

![img](https://gitee.com/tobing/imagebed/raw/master/137c593d-0a9e-47b8-a9e6-b71f540b82dd.png)

### IO 装饰器模式

以 InputStream 为例，

+ InputStream是抽象组件；
+ FileInputStream 是 InputStream 的子类，属于具体组件，提供字节流输入操作；
+ FilterInputStream 属于抽象装饰者，装饰者用于装饰组件，为组件提供额外的功能。如 BufferedInputStream 为 FileInputStream 提供缓存功能。

![image](https://gitee.com/tobing/imagebed/raw/master/DP-Decorator-java.io.png)

实例化一个具有缓存功能的字节流对象，只需要在 FileInputStream 对象上再套上一层 BufferedInpuStream 对象即可。

```java
FileInputStream fis = new FileInputStream(filePath);
BufferedInputStream bufferFIS = new BufferedInputStream(fis);
```

DataInputStream 装饰者提供了更多对继承数据类型进行输入的操作，比如 int、double等基本类型。

## 源码：InputStream

### InputStream 抽象类

InputStream 类

```java
// 读取数据
public abstract int read();

// 将读取到的数据放到byte数组，该方法实际上调用下面的方法实现，off=0,len=数组长度
public int read(byte b[]);

// 从第 off 的位置开始读取 len 长度字节的数据放到byte数组中，流是以-1来判断是否读取结束
public int read(byte b[], int off, int len);

// 跳过指定个数的字节不读取
public long skip(long n);

// 返回可读的字节数量
public int available() ;

// 读取完毕，关闭流，释放资源
public void close() ;

// 标记读取位置，下次还可以从这里开始读取，使用前看当前流是否支持，可以使用markSupport()判断
public synchronized void mark(int readlimit) ;

// 重置读取位置为上层 mark 标记的位置
public synchronized void reset() ;

// 判断当前流是否支持标记流，和了上述两个方法配套使用
public boolean markSupported() ;
```

### 源码实现

#### InputStream

```java
public abstract class InputStream implements Closeable {
    
	// 用于skip方法，和skipBuffer相关
    private static final int MAX_SKIP_BUFFER_SIZE = 2048;
    
    // 从输入流中读取下一个字节
    // 正常返回0~255，到达文件末尾返回-1
    // 在流中还有暑假，但是没有读到时该方法会阻塞（block）
    // Java IO和New IO的区别是阻塞流和非阻塞流
    // 抽象方法！不同的子类不同实现
    public abstract int read() throws IOException;

	// 利用下面方法实现，每次读取数组b的元素
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
    }

	// 将流中数据读入放到byte数组的第off个位置，相互len个位置中
    // 返回值为放入字节的个数。
    // 这个方法在利用持续那个方法read，某种意义上简单的Template模式
    public int read(byte b[], int off, int len) throws IOException {
        // 检验输入参数合法性
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        // 读取下一个字节
        int c = read();
        // 到达文件的末端返回-1
        if (c == -1) {
            return -1;
        }
		// 返回的字节downcast
        b[off] = (byte)c;
        // 已经读取了一个字节
        int i = 1;
        // 循环读取一个字节文件，放到byte中，知道读完len个字节或遇到-1
        try {
            for (; i < len ; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                b[off + i] = (byte)c;
            }
        } catch (IOException ee) {
        }
        return i;
    }

	// 方法内部使用，表示跳过的字节数目
    public long skip(long n) throws IOException {

        long remaining = n;
        int nr;

        if (n <= 0) { return 0; }
		// 初始化跳转缓存
        int size = (int)Math.min(MAX_SKIP_BUFFER_SIZE, remaining);
        byte[] skipBuffer = new byte[size];
        // 一共跳过n个，每次跳过部分，循环
        while (remaining > 0) {
            // 利用上面的read(byte[],int,int)方法尽量读取n个字节
            nr = read(skipBuffer, 0, (int)Math.min(size, remaining));
            if (nr < 0) { break;}
            // 没有读到需要部分，则继续循环
            remaining -= nr;
        }
		// 返回要么全部读完，要么因为到达文件末端，读取了部分
        return n - remaining;
    }

    // 查询流中还有多少可以读取的字节
    // 该方法不会block。在java中抽象方法的实现一般有以下几种方式：
    // 1）抛出异常，保证子类必须重写实现
    // 2）弱实现，推荐子类重写实现
    // 3）空实现
    public int available() throws IOException {
        return 0;
    }

	// 关闭当前流、同时释放与此流相关的资源
    public void close() throws IOException {}

    public synchronized void mark(int readlimit) {}

	// 对mark过的流进行复位。只有当前流支持mark才可以使用该方法
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

	// 查询当前流是否支持mark
    // 绝大部分不支持，因此提供默认实现，子类可以覆盖
    public boolean markSupported() { return false; }

}
```

#### FilterInputStream

```java
public class FilterInputStream extends InputStream {
    //装饰器的代码特征: 被装饰的对象一般是装饰器的成员变量
    protected volatile InputStream in; //将要被装饰的字节输入流
    protected FilterInputStream(InputStream in) {   //通过构造方法传入此被装饰的流
        this.in = in;
     }
    //下面这些方法，完成最小的装饰――0装饰，只是调用被装饰流的方法而已
    public int read() throws IOException {
        return in.read();
    }
    public int read(byte b[]) throws IOException {
        return read(b, 0, b.length);
     }
    public int read(byte b[], int off, int len) throws IOException {
        return in.read(b, off, len);
     }
    public long skip(long n) throws IOException {
        return in.skip(n);
    }
    public int available() throws IOException {
        return in.available();
    }
    public void close() throws IOException {
        in.close();
    }
    public synchronized void mark(int readlimit) {
        in.mark(readlimit);
     }
    public synchronized void reset() throws IOException {
        in.reset();
    }
    public boolean markSupported() {
        return in.markSupported();
    }
}
```

#### ByteArrayInputStream

```java
public class ByteArrayInputStream extends InputStream {
    protected byte buf[];	// 内部buffer，一般通过构造器输入
    protected int pos;		// 当前位置的cursor。从0~byte.lenght
    protected int mark = 0;	// mark的位置
    protected int count;	// 流中字节的数目
    // 根据 byte[] 创建对象
    public ByteArrayInputStream(byte buf[]) {
        this.buf = buf;
        this.pos = 0;
        this.count = buf.length;
    }
	// 构造器
    public ByteArrayInputStream(byte buf[], int offset, int length) {
        this.buf = buf;
        this.pos = offset;
        this.count = Math.min(offset + length, buf.length);
        this.mark = offset;
    }
	// 从流中读取下一个字节
    public synchronized int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }
	// ByteArrayInputStream覆盖了InputStream提供的read方法
    // 有时，父类不能完全实现子类的功能，父类的实现一般比子类通用
    // 当子类有更为有效的方法，可以覆盖这些方法。
    public synchronized int read(byte b[], int off, int len) {
        // 参数合法性校验
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        }
        if (pos >= count) { return -1; }
        int avail = count - pos;
        if (len > avail) { len = avail; }
        if (len <= 0) { return 0; }
        // Java提供的数据复制方法，这个方法速度很快
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }
	// 重写，提高效率
    public synchronized long skip(long n) {
        long k = count - pos;
        if (n < k) {
            k = n < 0 ? 0 : n;
        }
        pos += k;
        return k;
    }
	// 查询流中还有多少字节没有读取
    public synchronized int available() {        return count - pos;    }
	// ByteArrayInputStream支持mark
    public boolean markSupported() {        return true;    }
	// 获取流中当前mark的位置
    public void mark(int readAheadLimit) {        mark = pos;    }
	// 重置流，回到mark开始读
    public synchronized void reset() {        pos = mark;    }
	// 关闭ByteArrayInputStream不会有任何动作。
    public void close() throws IOException {    }
}
```

#### BufferedInputStream

```java
public class BufferedInputStream extends FilterInputStream {

    private static int DEFAULT_BUFFER_SIZE = 8192;				// 默认缓存大小
    private static int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;	// 最大缓存大小
    protected volatile byte buf[];	// 内部缓存
    // 原子性更新。和并发编程「一致性」有关
    private static final
        AtomicReferenceFieldUpdater<BufferedInputStream, byte[]> bufUpdater =
        AtomicReferenceFieldUpdater.newUpdater
        (BufferedInputStream.class,  byte[].class, "buf");
	
    protected int count;	// buffer大小
    protected int pos;		// buffer中cursor的位置
    protected int markpos = -1;	// mark的位置
    protected int marklimit;	// mark的范围
	// 检查输入流是否关闭，同时返回被包装的流
    private InputStream getInIfOpen() throws IOException {
        InputStream input = in;
        if (input == null)
            throw new IOException("Stream closed");
        return input;
    }
	// 检查buffer的状态，同时返回缓存
    private byte[] getBufIfOpen() throws IOException {
        byte[] buffer = buf;
        if (buffer == null)
            throw new IOException("Stream closed");
        return buffer;
    }
	// 构造器
    public BufferedInputStream(InputStream in) {
        this(in, DEFAULT_BUFFER_SIZE);
    }
	// 构造器：设置InputStream并创建buffer数组
    public BufferedInputStream(InputStream in, int size) {
        super(in);
        if (size <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        buf = new byte[size];
    }
	// 从流中读取数据，填充到缓存中
    private void fill() throws IOException {
        byte[] buffer = getBufIfOpen();
        if (markpos < 0)
            pos = 0;            /* no mark: throw away the buffer */
        else if (pos >= buffer.length)  /* no room left in buffer */
            if (markpos > 0) {  /* can throw away early part of the buffer */
                int sz = pos - markpos;
                System.arraycopy(buffer, markpos, buffer, 0, sz);
                pos = sz;
                markpos = 0;
            } else if (buffer.length >= marklimit) {
                markpos = -1;   /* buffer got too big, invalidate mark */
                pos = 0;        /* drop buffer contents */
            } else if (buffer.length >= MAX_BUFFER_SIZE) {
                throw new OutOfMemoryError("Required array size too large");
            } else {            /* grow buffer */
                int nsz = (pos <= MAX_BUFFER_SIZE - pos) ?
                        pos * 2 : MAX_BUFFER_SIZE;
                if (nsz > marklimit)
                    nsz = marklimit;
                byte nbuf[] = new byte[nsz];
                System.arraycopy(buffer, 0, nbuf, 0, pos);
                if (!bufUpdater.compareAndSet(this, buffer, nbuf)) {
                    // Can't replace buf if there was an async close.
                    // Note: This would need to be changed if fill()
                    // is ever made accessible to multiple threads.
                    // But for now, the only way CAS can fail is via close.
                    // assert buf == null;
                    throw new IOException("Stream closed");
                }
                buffer = nbuf;
            }
        count = pos;
        int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
        if (n > 0)
            count = n + pos;
    }
	// 读取下一个字节
    public synchronized int read() throws IOException {
        if (pos >= count) {
            fill();
            if (pos >= count)
                return -1;
        }
        return getBufIfOpen()[pos++] & 0xff;
    }

    // 将数据从流中读入buffer
    private int read1(byte[] b, int off, int len) throws IOException {
        int avail = count - pos;
        if (avail <= 0) {
            /* If the requested length is at least as large as the buffer, and
               if there is no mark/reset activity, do not bother to copy the
               bytes into the local buffer.  In this way buffered streams will
               cascade harmlessly. */
            if (len >= getBufIfOpen().length && markpos < 0) {
                return getInIfOpen().read(b, off, len);
            }
            fill();
            avail = count - pos;
            if (avail <= 0) return -1;
        }
        int cnt = (avail < len) ? avail : len;
        System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
        pos += cnt;
        return cnt;
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
        getBufIfOpen(); // Check for closed stream
        if ((off | len | (off + len) | (b.length - (off + len))) < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int n = 0;
        for (;;) {
            int nread = read1(b, off + n, len - n);
            if (nread <= 0)
                return (n == 0) ? nread : n;
            n += nread;
            if (n >= len)
                return n;
            // if not closed but no bytes available, return
            InputStream input = in;
            if (input != null && input.available() <= 0)
                return n;
        }
    }
    public synchronized long skip(long n) throws IOException {
        getBufIfOpen(); // Check for closed stream
        if (n <= 0) {
            return 0;
        }
        long avail = count - pos;
        if (avail <= 0) {
            // If no mark position set then don't keep in buffer
            if (markpos <0)
                return getInIfOpen().skip(n);

            // Fill in buffer to save bytes for reset
            fill();
            avail = count - pos;
            if (avail <= 0)
                return 0;
        }
        long skipped = (avail < n) ? avail : n;
        pos += skipped;
        return skipped;
    }
	// 不会阻塞，返回流中可读取的字节数目
    // 改方法返回值为缓存中的可读取字节数加流中可读字节数目的和
    public synchronized int available() throws IOException {
        int n = count - pos;
        int avail = getInIfOpen().available();
        return n > (Integer.MAX_VALUE - avail)
                    ? Integer.MAX_VALUE
                    : n + avail;
    }
	// 将当前位置设为mark位置
    public synchronized void mark(int readlimit) {
        marklimit = readlimit;
        markpos = pos;
    }
    public synchronized void reset() throws IOException {
        getBufIfOpen(); // Cause exception if closed
        if (markpos < 0)
            throw new IOException("Resetting to invalid mark");
        pos = markpos;
    }
	// 支持mark
    public boolean markSupported() {
        return true;
    }
	// 关闭当前流并释放相应系统资源
    public void close() throws IOException {
        byte[] buffer;
        while ( (buffer = buf) != null) {
            if (bufUpdater.compareAndSet(this, buffer, null)) {
                InputStream input = in;
                in = null;
                if (input != null)
                    input.close();
                return;
            }
            // Else retry in case a new buf was CASed in fill()
        }
    }
}
```

#### PipedInputStream

```java
public class PipedInputStream extends InputStream {
    boolean closedByWriter = false;				// 表示有读取方或写入方关闭
    volatile boolean closedByReader = false;	
    boolean connected = false;					// 是否建立连接
    Thread readSide;		// 	读线程
    Thread writeSide;		//	写线程
    private static final int DEFAULT_PIPE_SIZE = 1024;			// 缓冲区默认大小
    protected static final int PIPE_SIZE = DEFAULT_PIPE_SIZE; 	// 管道大小
    protected byte buffer[];// 缓冲区 
    protected int in = -1;	// 下一个写入字节的位置。0代表空，in==out代表满
    protected int out = 0;	// 下一个读取字节的位置
	// 构造器
    public PipedInputStream(PipedOutputStream src) throws IOException {
        this(src, DEFAULT_PIPE_SIZE);
    }
	// 构造器
    public PipedInputStream(PipedOutputStream src, int pipeSize)
            throws IOException {
         initPipe(pipeSize);
         connect(src);
    }
	// 构造器
    public PipedInputStream() {
        initPipe(DEFAULT_PIPE_SIZE);
    }
	// 构造器
    public PipedInputStream(int pipeSize) {
        initPipe(pipeSize);
    }
	// 初始化管道缓存
    private void initPipe(int pipeSize) {
         if (pipeSize <= 0) {
            throw new IllegalArgumentException("Pipe Size <= 0");
         }
         buffer = new byte[pipeSize];
    }
    // 连接输入端
    public void connect(PipedOutputStream src) throws IOException {
        src.connect(this);
    }
    // 内部使用，接收数据
    protected synchronized void receive(int b) throws IOException {
        checkStateForReceive();
        writeSide = Thread.currentThread();
        if (in == out)
            awaitSpace();
        if (in < 0) {
            in = 0;
            out = 0;
        }
        buffer[in++] = (byte)(b & 0xFF);
        if (in >= buffer.length) {
            in = 0;
        }
    }
    // 接收数据
    synchronized void receive(byte b[], int off, int len)  throws IOException {
        checkStateForReceive();
        writeSide = Thread.currentThread();
        int bytesToTransfer = len;
        while (bytesToTransfer > 0) {
            if (in == out)
                awaitSpace();
            int nextTransferAmount = 0;
            if (out < in) {
                nextTransferAmount = buffer.length - in;
            } else if (in < out) {
                if (in == -1) {
                    in = out = 0;
                    nextTransferAmount = buffer.length - in;
                } else {
                    nextTransferAmount = out - in;
                }
            }
            if (nextTransferAmount > bytesToTransfer)
                nextTransferAmount = bytesToTransfer;
            assert(nextTransferAmount > 0);
            System.arraycopy(b, off, buffer, in, nextTransferAmount);
            bytesToTransfer -= nextTransferAmount;
            off += nextTransferAmount;
            in += nextTransferAmount;
            if (in >= buffer.length) {
                in = 0;
            }
        }
    }
	// 检查当前状态，等待输入
    private void checkStateForReceive() throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByWriter || closedByReader) {
            throw new IOException("Pipe closed");
        } else if (readSide != null && !readSide.isAlive()) {
            throw new IOException("Read end dead");
        }
    }
	// Buffer已满，等待一段时间
    private void awaitSpace() throws IOException {
        while (in == out) {
            checkStateForReceive();

            /* full: kick any waiting readers */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
    }
	// 通知所有等待的线程已经接收到最后的字符
    synchronized void receivedLast() {
        closedByWriter = true;
        notifyAll();
    }

    public synchronized int read()  throws IOException {
        if (!connected) {
            throw new IOException("Pipe not connected");
        } else if (closedByReader) {
            throw new IOException("Pipe closed");
        } else if (writeSide != null && !writeSide.isAlive()
                   && !closedByWriter && (in < 0)) {
            throw new IOException("Write end dead");
        }

        readSide = Thread.currentThread();
        int trials = 2;
        while (in < 0) {
            if (closedByWriter) {
                /* closed by writer, return EOF */
                return -1;
            }
            if ((writeSide != null) && (!writeSide.isAlive()) && (--trials < 0)) {
                throw new IOException("Pipe broken");
            }
            /* might be a writer waiting */
            notifyAll();
            try {
                wait(1000);
            } catch (InterruptedException ex) {
                throw new java.io.InterruptedIOException();
            }
        }
        int ret = buffer[out++] & 0xFF;
        if (out >= buffer.length) {
            out = 0;
        }
        if (in == out) {
            /* now empty */
            in = -1;
        }

        return ret;
    }

    public synchronized int read(byte b[], int off, int len)  throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        /* possibly wait on the first character */
        int c = read();
        if (c < 0) {
            return -1;
        }
        b[off] = (byte) c;
        int rlen = 1;
        while ((in >= 0) && (len > 1)) {

            int available;

            if (in > out) {
                available = Math.min((buffer.length - out), (in - out));
            } else {
                available = buffer.length - out;
            }

            // A byte is read beforehand outside the loop
            if (available > (len - 1)) {
                available = len - 1;
            }
            System.arraycopy(buffer, out, b, off + rlen, available);
            out += available;
            rlen += available;
            len -= available;

            if (out >= buffer.length) {
                out = 0;
            }
            if (in == out) {
                /* now empty */
                in = -1;
            }
        }
        return rlen;
    }
	// 还有多少字节可以读取
    public synchronized int available() throws IOException {
        if(in < 0)
            return 0;
        else if(in == out)
            return buffer.length;
        else if (in > out)
            return in - out;
        else
            return in + buffer.length - out;
    }

    // 关闭当前流同时释放相关资源
    public void close()  throws IOException {
        closedByReader = true;
        synchronized (this) {
            in = -1;
        }
    }
}

```

## 源码：OutputStream 

### OutPutStream 抽象类

OutputStream抽象类

```java
public abstract void write(int b)
// 写入一个字节，可以看到这里的参数是一个 int 类型，对应上面的读方法，int 类型的 32 位，只有低 8 位才写入，高 24 位将舍弃。

public void write(byte b[])
// 将数组中的所有字节写入，和上面对应的 read() 方法类似，实际调用的也是下面的方法。

public void write(byte b[], int off, int len)
// 将 byte 数组从 off 位置开始，len 长度的字节写入

public void flush()
// 强制刷新，将缓冲中的数据写入

public void close()
// 关闭输出流，流被关闭后就不能再输出数据了
```

### 源码实现

FilterOutputStream

```java
/** * This class is the superclass of all classes that filter output * streams. These streams sit on top of an already existing output * stream (the <i>underlying</i> output stream) which it uses as its * basic sink of data, but possibly transforming the data along the * way or providing additional functionality. * <p> * The class <code>FilterOutputStream</code> itself simply overrides * all methods of <code>OutputStream</code> with versions that pass * all requests to the underlying output stream. Subclasses of * <code>FilterOutputStream</code> may further override some of these * methods as well as provide additional methods and fields. * * @author  Jonathan Payne * @since   JDK1.0 */publicclass FilterOutputStream extends OutputStream {    /**     * The underlying output stream to be filtered.     */    protected OutputStream out;    /**     * Creates an output stream filter built on top of the specified     * underlying output stream.     *     * @param   out   the underlying output stream to be assigned to     *                the field <tt>this.out</tt> for later use, or     *                <code>null</code> if this instance is to be     *                created without an underlying stream.     */    public FilterOutputStream(OutputStream out) {        this.out = out;    }    /**     * Writes the specified <code>byte</code> to this output stream.     * <p>     * The <code>write</code> method of <code>FilterOutputStream</code>     * calls the <code>write</code> method of its underlying output stream,     * that is, it performs <tt>out.write(b)</tt>.     * <p>     * Implements the abstract <tt>write</tt> method of <tt>OutputStream</tt>.     *     * @param      b   the <code>byte</code>.     * @exception  IOException  if an I/O error occurs.     */    public void write(int b) throws IOException {        out.write(b);    }    /**     * Writes <code>b.length</code> bytes to this output stream.     * <p>     * The <code>write</code> method of <code>FilterOutputStream</code>     * calls its <code>write</code> method of three arguments with the     * arguments <code>b</code>, <code>0</code>, and     * <code>b.length</code>.     * <p>     * Note that this method does not call the one-argument     * <code>write</code> method of its underlying stream with the single     * argument <code>b</code>.     *     * @param      b   the data to be written.     * @exception  IOException  if an I/O error occurs.     * @see        java.io.FilterOutputStream#write(byte[], int, int)     */    public void write(byte b[]) throws IOException {        write(b, 0, b.length);    }    /**     * Writes <code>len</code> bytes from the specified     * <code>byte</code> array starting at offset <code>off</code> to     * this output stream.     * <p>     * The <code>write</code> method of <code>FilterOutputStream</code>     * calls the <code>write</code> method of one argument on each     * <code>byte</code> to output.     * <p>     * Note that this method does not call the <code>write</code> method     * of its underlying input stream with the same arguments. Subclasses     * of <code>FilterOutputStream</code> should provide a more efficient     * implementation of this method.     *     * @param      b     the data.     * @param      off   the start offset in the data.     * @param      len   the number of bytes to write.     * @exception  IOException  if an I/O error occurs.     * @see        java.io.FilterOutputStream#write(int)     */    public void write(byte b[], int off, int len) throws IOException {        if ((off | len | (b.length - (len + off)) | (off + len)) < 0)            throw new IndexOutOfBoundsException();        for (int i = 0 ; i < len ; i++) {            write(b[off + i]);        }    }    /**     * Flushes this output stream and forces any buffered output bytes     * to be written out to the stream.     * <p>     * The <code>flush</code> method of <code>FilterOutputStream</code>     * calls the <code>flush</code> method of its underlying output stream.     *     * @exception  IOException  if an I/O error occurs.     * @see        java.io.FilterOutputStream#out     */    public void flush() throws IOException {        out.flush();    }    /**     * Closes this output stream and releases any system resources     * associated with the stream.     * <p>     * The <code>close</code> method of <code>FilterOutputStream</code>     * calls its <code>flush</code> method, and then calls the     * <code>close</code> method of its underlying output stream.     *     * @exception  IOException  if an I/O error occurs.     * @see        java.io.FilterOutputStream#flush()     * @see        java.io.FilterOutputStream#out     */    @SuppressWarnings("try")    public void close() throws IOException {        try (OutputStream ostream = out) {            flush();        }    }}
```

#### ByteArrayOutputStream

```java
/** * This class implements an output stream in which the data is * written into a byte array. The buffer automatically grows as data * is written to it. * The data can be retrieved using <code>toByteArray()</code> and * <code>toString()</code>. * <p> * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in * this class can be called after the stream has been closed without * generating an <tt>IOException</tt>. * * @author  Arthur van Hoff * @since   JDK1.0 */public class ByteArrayOutputStream extends OutputStream {    /**     * The buffer where data is stored.     */    protected byte buf[];    /**     * The number of valid bytes in the buffer.     */    protected int count;    /**     * Creates a new byte array output stream. The buffer capacity is     * initially 32 bytes, though its size increases if necessary.     */    public ByteArrayOutputStream() {        this(32);    }    /**     * Creates a new byte array output stream, with a buffer capacity of     * the specified size, in bytes.     *     * @param   size   the initial size.     * @exception  IllegalArgumentException if size is negative.     */    public ByteArrayOutputStream(int size) {        if (size < 0) {            throw new IllegalArgumentException("Negative initial size: "                                               + size);        }        buf = new byte[size];    }    /**     * Increases the capacity if necessary to ensure that it can hold     * at least the number of elements specified by the minimum     * capacity argument.     *     * @param minCapacity the desired minimum capacity     * @throws OutOfMemoryError if {@code minCapacity < 0}.  This is     * interpreted as a request for the unsatisfiably large capacity     * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.     */    private void ensureCapacity(int minCapacity) {        // overflow-conscious code        if (minCapacity - buf.length > 0)            grow(minCapacity);    }    /**     * The maximum size of array to allocate.     * Some VMs reserve some header words in an array.     * Attempts to allocate larger arrays may result in     * OutOfMemoryError: Requested array size exceeds VM limit     */    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;    /**     * Increases the capacity to ensure that it can hold at least the     * number of elements specified by the minimum capacity argument.     *     * @param minCapacity the desired minimum capacity     */    private void grow(int minCapacity) {        // overflow-conscious code        int oldCapacity = buf.length;        int newCapacity = oldCapacity << 1;        if (newCapacity - minCapacity < 0)            newCapacity = minCapacity;        if (newCapacity - MAX_ARRAY_SIZE > 0)            newCapacity = hugeCapacity(minCapacity);        buf = Arrays.copyOf(buf, newCapacity);    }    private static int hugeCapacity(int minCapacity) {        if (minCapacity < 0) // overflow            throw new OutOfMemoryError();        return (minCapacity > MAX_ARRAY_SIZE) ?            Integer.MAX_VALUE :            MAX_ARRAY_SIZE;    }    /**     * Writes the specified byte to this byte array output stream.     *     * @param   b   the byte to be written.     */    public synchronized void write(int b) {        ensureCapacity(count + 1);        buf[count] = (byte) b;        count += 1;    }    /**     * Writes <code>len</code> bytes from the specified byte array     * starting at offset <code>off</code> to this byte array output stream.     *     * @param   b     the data.     * @param   off   the start offset in the data.     * @param   len   the number of bytes to write.     */    public synchronized void write(byte b[], int off, int len) {        if ((off < 0) || (off > b.length) || (len < 0) ||            ((off + len) - b.length > 0)) {            throw new IndexOutOfBoundsException();        }        ensureCapacity(count + len);        System.arraycopy(b, off, buf, count, len);        count += len;    }    /**     * Writes the complete contents of this byte array output stream to     * the specified output stream argument, as if by calling the output     * stream's write method using <code>out.write(buf, 0, count)</code>.     *     * @param      out   the output stream to which to write the data.     * @exception  IOException  if an I/O error occurs.     */    public synchronized void writeTo(OutputStream out) throws IOException {        out.write(buf, 0, count);    }    /**     * Resets the <code>count</code> field of this byte array output     * stream to zero, so that all currently accumulated output in the     * output stream is discarded. The output stream can be used again,     * reusing the already allocated buffer space.     *     * @see     java.io.ByteArrayInputStream#count     */    public synchronized void reset() {        count = 0;    }    /**     * Creates a newly allocated byte array. Its size is the current     * size of this output stream and the valid contents of the buffer     * have been copied into it.     *     * @return  the current contents of this output stream, as a byte array.     * @see     java.io.ByteArrayOutputStream#size()     */    public synchronized byte toByteArray()[] {        return Arrays.copyOf(buf, count);    }    /**     * Returns the current size of the buffer.     *     * @return  the value of the <code>count</code> field, which is the number     *          of valid bytes in this output stream.     * @see     java.io.ByteArrayOutputStream#count     */    public synchronized int size() {        return count;    }    /**     * Converts the buffer's contents into a string decoding bytes using the     * platform's default character set. The length of the new <tt>String</tt>     * is a function of the character set, and hence may not be equal to the     * size of the buffer.     *     * <p> This method always replaces malformed-input and unmappable-character     * sequences with the default replacement string for the platform's     * default character set. The {@linkplain java.nio.charset.CharsetDecoder}     * class should be used when more control over the decoding process is     * required.     *     * @return String decoded from the buffer's contents.     * @since  JDK1.1     */    public synchronized String toString() {        return new String(buf, 0, count);    }    /**     * Converts the buffer's contents into a string by decoding the bytes using     * the named {@link java.nio.charset.Charset charset}. The length of the new     * <tt>String</tt> is a function of the charset, and hence may not be equal     * to the length of the byte array.     *     * <p> This method always replaces malformed-input and unmappable-character     * sequences with this charset's default replacement string. The {@link     * java.nio.charset.CharsetDecoder} class should be used when more control     * over the decoding process is required.     *     * @param      charsetName  the name of a supported     *             {@link java.nio.charset.Charset charset}     * @return     String decoded from the buffer's contents.     * @exception  UnsupportedEncodingException     *             If the named charset is not supported     * @since      JDK1.1     */    public synchronized String toString(String charsetName)        throws UnsupportedEncodingException    {        return new String(buf, 0, count, charsetName);    }    /**     * Creates a newly allocated string. Its size is the current size of     * the output stream and the valid contents of the buffer have been     * copied into it. Each character <i>c</i> in the resulting string is     * constructed from the corresponding element <i>b</i> in the byte     * array such that:     * <blockquote><pre>     *     c == (char)(((hibyte &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))     * </pre></blockquote>     *     * @deprecated This method does not properly convert bytes into characters.     * As of JDK&nbsp;1.1, the preferred way to do this is via the     * <code>toString(String enc)</code> method, which takes an encoding-name     * argument, or the <code>toString()</code> method, which uses the     * platform's default character encoding.     *     * @param      hibyte    the high byte of each resulting Unicode character.     * @return     the current contents of the output stream, as a string.     * @see        java.io.ByteArrayOutputStream#size()     * @see        java.io.ByteArrayOutputStream#toString(String)     * @see        java.io.ByteArrayOutputStream#toString()     */    @Deprecated    public synchronized String toString(int hibyte) {        return new String(buf, hibyte, 0, count);    }    /**     * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in     * this class can be called after the stream has been closed without     * generating an <tt>IOException</tt>.     */    public void close() throws IOException {    }}
```

#### BufferedOutputStream

```java
/** * The class implements a buffered output stream. By setting up such * an output stream, an application can write bytes to the underlying * output stream without necessarily causing a call to the underlying * system for each byte written. * * @author  Arthur van Hoff * @since   JDK1.0 */publicclass BufferedOutputStream extends FilterOutputStream {    /**     * The internal buffer where data is stored.     */    protected byte buf[];    /**     * The number of valid bytes in the buffer. This value is always     * in the range <tt>0</tt> through <tt>buf.length</tt>; elements     * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid     * byte data.     */    protected int count;    /**     * Creates a new buffered output stream to write data to the     * specified underlying output stream.     *     * @param   out   the underlying output stream.     */    public BufferedOutputStream(OutputStream out) {        this(out, 8192);    }    /**     * Creates a new buffered output stream to write data to the     * specified underlying output stream with the specified buffer     * size.     *     * @param   out    the underlying output stream.     * @param   size   the buffer size.     * @exception IllegalArgumentException if size &lt;= 0.     */    public BufferedOutputStream(OutputStream out, int size) {        super(out);        if (size <= 0) {            throw new IllegalArgumentException("Buffer size <= 0");        }        buf = new byte[size];    }    /** Flush the internal buffer */    private void flushBuffer() throws IOException {        if (count > 0) {            out.write(buf, 0, count);            count = 0;        }    }    /**     * Writes the specified byte to this buffered output stream.     *     * @param      b   the byte to be written.     * @exception  IOException  if an I/O error occurs.     */    public synchronized void write(int b) throws IOException {        if (count >= buf.length) {            flushBuffer();        }        buf[count++] = (byte)b;    }    /**     * Writes <code>len</code> bytes from the specified byte array     * starting at offset <code>off</code> to this buffered output stream.     *     * <p> Ordinarily this method stores bytes from the given array into this     * stream's buffer, flushing the buffer to the underlying output stream as     * needed.  If the requested length is at least as large as this stream's     * buffer, however, then this method will flush the buffer and write the     * bytes directly to the underlying output stream.  Thus redundant     * <code>BufferedOutputStream</code>s will not copy data unnecessarily.     *     * @param      b     the data.     * @param      off   the start offset in the data.     * @param      len   the number of bytes to write.     * @exception  IOException  if an I/O error occurs.     */    public synchronized void write(byte b[], int off, int len) throws IOException {        if (len >= buf.length) {            /* If the request length exceeds the size of the output buffer,               flush the output buffer and then write the data directly.               In this way buffered streams will cascade harmlessly. */            flushBuffer();            out.write(b, off, len);            return;        }        if (len > buf.length - count) {            flushBuffer();        }        System.arraycopy(b, off, buf, count, len);        count += len;    }    /**     * Flushes this buffered output stream. This forces any buffered     * output bytes to be written out to the underlying output stream.     *     * @exception  IOException  if an I/O error occurs.     * @see        java.io.FilterOutputStream#out     */    public synchronized void flush() throws IOException {        flushBuffer();        out.flush();    }}
```

#### PipedOutputStream

```java
/** * A piped output stream can be connected to a piped input stream * to create a communications pipe. The piped output stream is the * sending end of the pipe. Typically, data is written to a * <code>PipedOutputStream</code> object by one thread and data is * read from the connected <code>PipedInputStream</code> by some * other thread. Attempting to use both objects from a single thread * is not recommended as it may deadlock the thread. * The pipe is said to be <a name=BROKEN> <i>broken</i> </a> if a * thread that was reading data bytes from the connected piped input * stream is no longer alive. * * @author  James Gosling * @see     java.io.PipedInputStream * @since   JDK1.0 */publicclass PipedOutputStream extends OutputStream {        /* REMIND: identification of the read and write sides needs to be           more sophisticated.  Either using thread groups (but what about           pipes within a thread?) or using finalization (but it may be a           long time until the next GC). */    private PipedInputStream sink;    /**     * Creates a piped output stream connected to the specified piped     * input stream. Data bytes written to this stream will then be     * available as input from <code>snk</code>.     *     * @param      snk   The piped input stream to connect to.     * @exception  IOException  if an I/O error occurs.     */    public PipedOutputStream(PipedInputStream snk)  throws IOException {        connect(snk);    }    /**     * Creates a piped output stream that is not yet connected to a     * piped input stream. It must be connected to a piped input stream,     * either by the receiver or the sender, before being used.     *     * @see     java.io.PipedInputStream#connect(java.io.PipedOutputStream)     * @see     java.io.PipedOutputStream#connect(java.io.PipedInputStream)     */    public PipedOutputStream() {    }    /**     * Connects this piped output stream to a receiver. If this object     * is already connected to some other piped input stream, an     * <code>IOException</code> is thrown.     * <p>     * If <code>snk</code> is an unconnected piped input stream and     * <code>src</code> is an unconnected piped output stream, they may     * be connected by either the call:     * <blockquote><pre>     * src.connect(snk)</pre></blockquote>     * or the call:     * <blockquote><pre>     * snk.connect(src)</pre></blockquote>     * The two calls have the same effect.     *     * @param      snk   the piped input stream to connect to.     * @exception  IOException  if an I/O error occurs.     */    public synchronized void connect(PipedInputStream snk) throws IOException {        if (snk == null) {            throw new NullPointerException();        } else if (sink != null || snk.connected) {            throw new IOException("Already connected");        }        sink = snk;        snk.in = -1;        snk.out = 0;        snk.connected = true;    }    /**     * Writes the specified <code>byte</code> to the piped output stream.     * <p>     * Implements the <code>write</code> method of <code>OutputStream</code>.     *     * @param      b   the <code>byte</code> to be written.     * @exception IOException if the pipe is <a href=#BROKEN> broken</a>,     *          {@link #connect(java.io.PipedInputStream) unconnected},     *          closed, or if an I/O error occurs.     */    public void write(int b)  throws IOException {        if (sink == null) {            throw new IOException("Pipe not connected");        }        sink.receive(b);    }    /**     * Writes <code>len</code> bytes from the specified byte array     * starting at offset <code>off</code> to this piped output stream.     * This method blocks until all the bytes are written to the output     * stream.     *     * @param      b     the data.     * @param      off   the start offset in the data.     * @param      len   the number of bytes to write.     * @exception IOException if the pipe is <a href=#BROKEN> broken</a>,     *          {@link #connect(java.io.PipedInputStream) unconnected},     *          closed, or if an I/O error occurs.     */    public void write(byte b[], int off, int len) throws IOException {        if (sink == null) {            throw new IOException("Pipe not connected");        } else if (b == null) {            throw new NullPointerException();        } else if ((off < 0) || (off > b.length) || (len < 0) ||                   ((off + len) > b.length) || ((off + len) < 0)) {            throw new IndexOutOfBoundsException();        } else if (len == 0) {            return;        }        sink.receive(b, off, len);    }    /**     * Flushes this output stream and forces any buffered output bytes     * to be written out.     * This will notify any readers that bytes are waiting in the pipe.     *     * @exception IOException if an I/O error occurs.     */    public synchronized void flush() throws IOException {        if (sink != null) {            synchronized (sink) {                sink.notifyAll();            }        }    }    /**     * Closes this piped output stream and releases any system resources     * associated with this stream. This stream may no longer be used for     * writing bytes.     *     * @exception  IOException  if an I/O error occurs.     */    public void close()  throws IOException {        if (sink != null) {            sink.receivedLast();        }    }}
```

## 常见类的使用

Java I/O 可以分为以下几类：

+ 磁盘操作：File
+ 字节操作：InputStream 和 OutputStream
+ 字符操作：Reader 和 Writer
+ 独享操作：Serializable
+ 为了操作：Socket

### File

File 类可以用于表示文件和目录，但不表示文件内容。

### 字节流相关

//TODO

### 序列化 

// TODO

### Socket

// TODO

## Unix IO 模型

### 简介

一个输入操作通常包含两个阶段：

+ 等待数据准备好； 
+ 从内核向进程复制数据；

对于一个套接字上的输入操作，第一步通常涉及等待数据从网络中到达。到所有等待分组到达时，它被复制到系统内核的某个缓冲区。第二步就是把数据从内核缓冲区复制到应用程序缓冲区中。

Unix 有五种 I/O 模型：

+ 阻塞式 I/O
+ 非阻塞式 I/O
+ I/O 复用（select 和 poll）
+ 信号驱动式 I/O（SIGIO）
+ 异步 I/O（AIO）

### 阻塞式 I/O

应用程序被阻塞，直到数据复制到应用程序缓冲区才返回。

在阻塞过程中，其他程序还可以执行，因此阻塞不意味着整个操作系统被阻塞。因为其他程序还可以被执行，因此不消耗 CPU 时间，这种模型执行效率会比较高。

下图中，recvfrom 用于接收 Socket 传来的数据，并复制到应用程序的缓冲区 buf 中。这里把recvfrom 当成系统调用。

```java
ssize_t recvfrom(int sockfd, void *buf, size_t len, int flags, struct *src_add, socklen_t *addrlen);
```

![img](https://gitee.com/tobing/imagebed/raw/master/java-io-model-0.png)

### 非阻塞式 I/O

应用程序执行系统调用，内核返回一个错误码。应用程序可以继续执行，但需要不断执行系统调用来获知 I/O 是否完成，这种方式称为轮询（polling）。

由于 CPU 要处理更多的系统调用，因此这种模型是比较低效的。

![img](https://gitee.com/tobing/imagebed/raw/master/java-io-model-1.png)

### I/O复用

使用 select 或 poll 等待数据，并且可以等待多个套接字中的任何一个变为可读，这一过程会被阻塞，当某个套接字可读时返回。之后在使用 recvfrom 把数据从内核复制到进程中。

它可以让单个进程具有处理多个 I/O 事件的能力。又被称为 Event Driven I/O，即事件驱动 I/O。

如果一个 Web 服务器没有 I/O 复用，那么每个 Socket 连接都需要创建一个线程去处理。如果同时有几万个连接，那么就需要创建相同数量的线程。并且相比于多进程和多线程技术，I/O 复用不需要进程线程创建和切换的开销，系统开销小。

![img](https://gitee.com/tobing/imagebed/raw/master/java-io-model-2.png)

### 信号驱动 I/O

应用进程使用 sigaction 系统调用，内核立即返回，应用进程可以继续执行，也就是说等待数据阶段应用进程是非阻塞的。内核在数据到达时先应用进程发送 SIGIO 信号，应用进程收到之后在信号处理程序中调用 recvfrom 将数据从内核复制到应用进程中。

相比于阻塞式 I/O 的轮询方式，信号驱动 I/O 的 CPU 利用率更高。

![img](https://gitee.com/tobing/imagebed/raw/master/java-io-model-3.png)

### 异步 I/O

进行 aio_read 系统调用会立即返回，应用进程继续执行，不会被阻塞，内核会在所有操作完成之后向应用进程发送信号。

异步 I/O 与 信号驱动 I/O 区别在于，异步 I/O 的信号是通知应用进程 I/O 完成，而信号驱动 I/O 的信号是通知应用进程可以开始 I/O。

![img](https://gitee.com/tobing/imagebed/raw/master/java-io-model-4.png)

### I/O 模型比较

#### 同步 I/O 与 异步 I/O

+ 同步 I/O：应用进程在调用 recvfrom 操作是会阻塞
+ 异步 I/O：不会阻塞

阻塞式 I/O、非阻塞式 I/O、I/O 复用和信号驱动 I/O 都是同步 I/O，虽然非阻塞式 I/O 和信号驱动 I/O 在等待数据阶段不会阻塞，但是只会将数据从内核复制到应用进程这个操作会阻塞。

#### 五大 I/O 模型比较

前四种I/O模型主要区别在第一阶段，而第二阶段是一样的：将数据从内核复制到应用进程过程中，应用程序会被阻塞。

![img](https://gitee.com/tobing/imagebed/raw/master/1492928105791_3.png)

### IO 多路复用

#### IO 多路复用工作模式

epoll 的描述符事件有两种触发模式：LT(level trigger) 和 ET(edge trigger)。

+ **LT模式**：当epoll_wait() 检查当前描述符事件到达，将时间通知进程，进程可以不立即处理该事件，下次调用 epoll_wait() 会再次通知进程。是默认的一种模式，比同时支持 Blocking 和 No-Blocking。
+ **ET模式**：和LT模式不同，通知之后继承必须立即处理事件，下次再调用 epoll_wait() 时不会再得到事件到达的通知。

ET模式很大程度减少了 epoll 事件被重复触发的次数，因此效率要比 LT 模式高。只支持 No-Blocking，以避免由于一个文件句柄的阻塞读/阻塞写操作包处理多个文件描述的任务饿死。

很容易产生一种错觉认为只要用 epoll 即可， select 和 poll 都过时。但实际上它们都有各自的使用场景。

**【select 应用场景】**

select 的timeout 参数精度为 1ns，而 poll 和 epoll 为 1ms，因此 select 更适用于实时要求更高的场景，比如核反应堆控制。

select 可移植性更好，几乎被所有主流平台支持。

**【poll 应用场景】**

poll 没有最大描述符数量的心智，如果平台支持并且对实时性要求不高，应该使用 poll 而不是 select。

需要同时监控小于 1000 个描述符，就没有必要使用 epoll，因为这个场景下并不能体现 epoll 的优势。

需要监控的描述符变化多，而且都是非常短暂的，也没有必要使用 epoll。因为 epoll 中的所有描述符都存储在内核中，造成每次需要对描述符的状态改变都需要通过 epoll_ctl 进行系统调用，频繁系统调用降低效率。并且 epoll 的描述符储存在内核，不容易调试。

**【epoll 应用场景】**

只需要运行在 Linux 平台上，并且有大量的描述符需要同时轮询，而且这些连接最后是长连接。

##  BIO 详解

BIO就是：Blocking IO。最容易理解、最容易实现的IO工作方式，应用程序向操作系统请求为了IO操作，这是应用程序会一直等待；另一方面，操作系统收到请求后，也会等待，直到为了上所有数据传到监听端口；操作系统在收集数据后，会把数据发送给应用程序；最后应用程序收到数据，并解除等待状态。

### 几个重要概念

【阻塞 IO 和 非阻塞 IO】

这两个概念是程序级别。主要描述程序请求操作系统IO操作后，如果IO资源还没有准备好，那么程序该如何处理的问题：前者等待；后者继续执行（并且使用线程一直轮询，知道有IO资源准备好了）

【同步 IO 和 异步 IO】

这个概念是操作系统级别。主要描述操作系统在收到程序请求IO操作后，如果IO资源还没有准备好，该如何响应程序的问题：前者不响应，直到IO资源准备好以后；后者返回一个标记（好让程序和自己知道以后的数据往哪里通知），当IO资源准备好之后，再用事件机制返回给程序。

### 传统BIO通信方式

以前大多数为了通信方式都是阻塞模式，即：

+ 客户端向服务器端发出请求后，客户端会一直等待，直到服务器返回结果或网络出现问题；
+ 服务器端同样，当处理某个客户端A发送的请求时，另一个客户端B发来的请求会等待，直到服务器端的这个处理线程完上一个处理。

#### 传统BIO存在的问题

+ 同一时间，服务器只能接受来自客户端A请求信息；虽然客户端A和客户端B的请求时同时进行的，但客户端B发送的请求只能等待服务器接受完A的请求之后，才能被接受。
+ 由于服务器异常只能处理一个客户端请求，当处理完并返回后，才能进行第二次请求的处理。显然，这样的处理方式在高并发情况下，是不能采用的。

#### 多线程-伪异步方式

上面提到的情况是服务器只有一个线程的情况，现在考虑使用多线程技术来解决这个问题：

+ 当服务器收到客户端X的请求后，（读取到所有请求数据后）将这个请求送入一个独立线程进行处理，然后主线程机型接收客户端Y的请求。
+ 客户端一侧，也可以使用一个子线程和服务器端进行通信。主要客户端主线程的其他工作可以不受影响，当服务器端有响应信息的时候再由这个子线程通过「监听模式/观察模式(等其他设计模式)」通知主线程。

但实际上，使用线程来解决问题有局限性：

+ 虽然服务器端，请求的处理交给一个独立线程进行，但是操作系统通知accept的方式还是单个。也就是说，实际上服务器接收到数据报文后的「业务处理过程」可以多线程，但是数据报文的接受还是需要一个一个的来；
+ 在linux中，可以创建的线程有限。可以通过/proc/sys/kernel/threads-max命令查看可以几创建的最大线程数。虽然这个值可以修改，但是线程越多，CPU切换需要的时间也就越多，用于真正业务处理的需求就越少；
+ 创建一个线程有较大的资源消耗。JVM创建一个线程的时候，即时这个线程不做任何工作，JVM都会分配一个堆栈空间。这个空间的大小默认为128K，可以通过-Xss参数可以调整。还可以使用ThreadPoolExecutor线程池来缓解线程的创建问题，但会造成BlockingQueue积压任务的持续增加，同时消耗大量的资源；
+ 另外，如果应用程序使用大量的长连接，线程是不会关闭的。这样系统资源的消耗更容易失控。那么如果单纯使用线程解决阻塞问题，并不是最后的解决办法。

#### BIO通信方式深入分析

BIO的问题不在于是否使用了多线程处理请求，而是在于accept、read的操作都是被阻塞。

socekt套接字的IO模式支持是基于操作系统的，同步IO/异步IO的支持也需要操作系统。

服务器线程在发起一个accept动作，询问操作系统是否有新的socket套接字信息从端口X发送过来。如果Java程序没有设置timeout，那么Java程序在调用JNI是，会一直等待，直到有数据返回。

## Java NIO

传统的IO基于字节流进行读写，在进行IO之前，实现创建一个流对象，流对象进行读写操作都是按字节，一个字节一个字节的来读或写。而NIO把IO出现为块，类似磁盘的读写，每次IO操作的单位都是一个块，块被读入内存之后就是有个byte[]，NIO一次可以读或写多个字节。

### 流与块

I/O与NIO最重要的区别在于数据的打包方式和传输方式，I/O以流的方式处理数据，而NIO以块的方式处理数据。

面向流的 I/O 一次处理一个字节数据: 一个输入流产生一个字节数据，一个输出流消费一个字节数据。为流式数据创建过滤器非常容易，链接几个过滤器，以便每个过滤器只负责复杂处理机制的一部分。不利的一面是，面向流的 I/O 通常相当慢。

面向块的 I/O 一次处理一个数据块，按块处理数据比按流处理数据要快得多。但是面向块的 I/O 缺少一些面向流的 I/O 所具有的优雅性和简单性。

I/O 包和 NIO 已经很好地集成了，java.io.* 已经以 NIO 为基础重新实现了，所以现在它可以利用 NIO 的一些特性。例如，java.io.* 包中的一些类包含以块的形式读写数据的方法，这使得即使在面向流的系统中，处理速度也会更快。

### 通道与缓冲区

#### 通道

Channel是对原 I/O包中流的模拟，可以它能够给它读取和写入数据。

通道与流的不同之处在于，流只能在一个方向上移动(一个流必须是 InputStream 或者 OutputStream 的子类)，而通道是双向的，可以用于读、写或者同时用于读写。

通道通畅包含以下类型：

+ FileChnnel：从文件中读写数据；

+ DatagramChannel：从UDP读写网络中数据；

+ SocketChannel：从TCP读写网络中数据；

+ ServerSocketChannel：可以监听新进来的TCP连接，对每个新来的连接创建一个SocketChannel。

#### 缓冲区

发送给一个通达的所有数据都必须实现放到缓冲区中，同样地，从通道中读取的任何数据都要先读到缓冲区。即，不会会直接对通道读写数据，要先经过缓冲区。

缓冲区实际上是一个数组，但不仅仅是一个数组。缓冲区提供了对数据结构化访问，而且可以通过跟踪系统的读写进程。

缓冲区包括以下类型：

+ ByteBuffer
+ CharBuffer
+ ShortBuffer
+ IntBuffer
+ LongBuffer
+ FloatBuffer
+ DoubleBuffer

#### 缓冲区状态变量

缓冲区中存在以下的状态变量：

+ capacity: 最大容量；
+ position: 当前已经读写的字节数；
+ limit: 还可以读写的字节数。

#### 选择器

NIO 常常被称为非阻塞IO，主要因为NIO在网络通信中的非阻塞特性被广泛使用。

NIO 实现了 IO 多路复用中的 Reactor 模型，一个线程Thread使用一个选择器 Selector 通过轮询的方式去监听多个通道 Channel 上的事件，从而让一个线程可以处理多个事件。

通过配置监听的通道 Channel 为非阻塞，当 Channel 上的 IO事件还未到达时，就不会进入阻塞状态一直等待，而是继续论述其他 Channel，找到 IO事件已经到达的 Channel 执行。

因为创建和切换线程开销大，因此使用一个线程处理多个事件而不是一个线程处理一个事件具有更好的性能。应该注意，只有套接字 Channel 才能配置为非阻塞，而 FileChannel 不能，为 FileChannel 配置配置非阻塞也没有意义。

```java
// 创建选择器
Selector selector = Selector.open();

// 创建套接字通道
ServerSocektChannel ssChannel = ServerSocketChannel.open();
// 将通道配置为非阻塞
ssChannel.configureBlocking(false);
// 将通道注册到选择器上
ssChannel.register(selector, SelectionKey.OP_ACCEPT);
```

通道必须配置为非阻塞模式，否则使用选择器就没有任何意义了，因为如果通道在某个事件上被阻塞，那么服务器就不能响应其它事件，必须等待这个事件处理完毕才能去处理其它事件，显然这和选择器的作用背道而驰。

著作权归https://pdai.tech所有。 链接：https://www.pdai.tech/md/java/io/java-io-nio.html

在将通道注册到选择器上时，还需要指定要注册的具体事件，主要有以下几类:

- SelectionKey.OP_CONNECT
- SelectionKey.OP_ACCEPT
- SelectionKey.OP_READ
- SelectionKey.OP_WRITE

每个事件可以被当成一个位域，从而组成事件集整数。例如:

```java
int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
```

#### 监听事件

```java
int num = selector.select();
```

使用 select() 来监听到达的事件，它会一直阻塞直到有至少一个事件到达。

#### 获取到达的事件

```java
Set<SelectionKey> keys = selector.selectedKeys();
Iterator<SelectionKey> keyIterator = keys.iterator();
while (keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
    if (key.isAcceptable()) {
        // ...
    } else if (key.isReadable()) {
        // ...
    }
    keyIterator.remove();
}
```

#### 事件循环

```java
while (true) {
    int num = selector.select();
    Set<SelectionKey> keys = selector.selectedKeys();
    Iterator<SelectionKey> keyIterator = keys.iterator();
    while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        if (key.isAcceptable()) {
            // ...
        } else if (key.isReadable()) {
            // ...
        }
        keyIterator.remove();
    }
}
```

#### 内存映射文件

内存映射文件I/O是一种读和写文件数据的方法，可以比常规的基于流和基于通道的I/O快得多。

向内存映射文件写入可能是危险的，只是改变数组的单个元素之一的简单操作，就可能会直接修改磁盘上的文件。修改数据与将数据保存到磁盘是没有分开的。

下面代码行将文件的前1024个字节映射到内存中，map方法返回一个MappedByteBuffer，是ByteBuffer子类。因此，可以像使用其他任何 ByteBuffer 一样使用新映射的缓冲区，操作系统会在需要是负责执行映射。

```java
MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_WRITE, 0, 1024);
```

## IO 多路复用详解

目前流程的多路复用IO实现主要包含四种：select、poll、epoll、kqueue。

| IO模型 | 相对性能 | 关键思路         | 操作系统     | Java支持情况                                                 |
| ------ | -------- | ---------------- | ------------ | ------------------------------------------------------------ |
| select | 较高     | Reactor          | Window/Linux | 支持,Reactor模式(反应器设计模式)。Linux操作系统的 kernels 2.4内核版本之前，默认使用select；而目前windows下对同步IO的支持，都是select模型 |
| poll   | 较高     | Reactor          | Linux        | Linux下的JAVA NIO框架，Linux kernels 2.6内核版本之前使用poll进行支持。也是使用的Reactor模式 |
| epoll  | 高       | Reactor/Proactor | Linux        | Linux kernels 2.6内核版本及以后使用epoll进行支持；Linux kernels 2.6内核版本之前使用poll进行支持；另外一定注意，由于Linux下没有Windows下的IOCP技术提供真正的 异步IO 支持，所以Linux下使用epoll模拟异步IO |
| kqueue | 高       | Proactor         | Linux        | 目前JAVA的版本不支持                                         |

多路复用IO技术最适用的是“高并发”场景，所谓高并发是指1毫秒内至少同时有上千个连接请求准备好。其他情况下多路复用IO技术发挥不出来它的优势。另一方面，使用JAVA NIO进行功能实现，相对于传统的Socket套接字实现要复杂一些，所以实际应用中，需要根据自己的业务需求进行技术选择。

### Reactor与Proactor

#### 传统IO模型

对于传统IO模型，其主要是一个Server对接N个客户端，在客户端连接之后，为每个客户端都分配一个执行线程。传统的IO特点在于：

+ 每个客户端连接到达之后，服务端会分配一个线程给该客户端，该线程会处理包括读取数据，解码，业务计算，编码，以及发送数据整个过程；
+ 同一时刻，服务端的吞吐量与服务器所提供的线程数量是呈线性关系的。

这种模式在客户端连接不多，并发量不大的情况下可以运行的很好，但在海量并发的情况下，这种模式就显得力不从心。这种模式主要存在的问题有如下几点：

+ 服务器的并发量对服务端能够创建的线程数有很大的依赖关系，但是服务器线程却是不能无限增长的；
+ 服务端每个线程不仅要进行IO读写操作，而且还需要进行业务计算；
+ 服务端在获取客户端连接，读取数据，以及写入数据的过程都是阻塞类型的，在网络状况不好的情况下，这将极大的降低服务器每个线程的利用率，从而降低服务器吞吐量。

#### Reactor事件驱动模型

在传统IO模型中，由于线程在等待连接以及进行IO操作时都会阻塞当前线程，这部分损耗是非常大的。因而jdk 1.4中就提供了一套非阻塞IO的API。该API本质上是以事件驱动来处理网络事件的，而Reactor是基于该API提出的一套IO模型。

在Reactor模型中，主要有四个角色：客户端连接，Reactor，Acceptor和Handler。这里Acceptor会不断地接收客户端的连接，然后将接收到的连接交由Reactor进行分发，最后有具体的Handler进行处理。改进后的Reactor模型相对于传统的IO模型主要有如下优点：

- 从模型上来讲，如果仅仅还是只使用一个线程池来处理客户端连接的网络读写，以及业务计算，那么Reactor模型与传统IO模型在效率上并没有什么提升。但是Reactor模型是以事件进行驱动的，其能够将接收客户端连接，+ 网络读和网络写，以及业务计算进行拆分，从而极大的提升处理效率；
- Reactor模型是异步非阻塞模型，工作线程在没有网络事件时可以处理其他的任务，而不用像传统IO那样必须阻塞等待。

在上面的Reactor模型中，由于网络读写和业务操作都在同一个线程中，在高并发情况下，这里的系统瓶颈主要在两方面：

- 高频率的网络读写事件处理；
- 大量的业务操作处理；

#### Reactor模型-业务处理与IO分离

基于上述两个问题，这里在单线程Reactor模型的基础上提出了使用线程池的方式处理业务操作的模型。

## 拓展思考

#### [并发读取大文件](https://www.codenong.com/11867348/)


