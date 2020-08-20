import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CustomOperation {

	String FFMPEG_PATH = "/usr/bin/ffmpeg";

	public void runBeforeInit(String[] args) {
		// System.out.println("运行在初始化参数之前");
	}

	public void runAfterInit() {
		// System.out.println("运行在初始化参数之后");
	}

	public void runAfterComplete(List<File> files) throws IOException {
		System.out.println("运行在录制完毕，时间戳处理完后");
		for (File f : files) {
			System.out.printf("分离音频: %s \r\n", f.getCanonicalPath());
			File aacFile = new File(f.getParentFile(), f.getName().replaceFirst("flv$", "aac"));
			String[] cmd = { "/bin/bash", "-c", FFMPEG_PATH + " -i " + f.getCanonicalPath() + " -vn -acodec copy " + aacFile.getCanonicalPath() + " && rm " + f.getCanonicalPath() };
			run(cmd);
		}
	}

	boolean run(String[] cmd) {
		try {
			System.out.println("process 执行开始");
			final Process process = Runtime.getRuntime().exec(cmd);
			StreamManager errorStream = new StreamManager(process, process.getErrorStream());
			StreamManager outputStream = new StreamManager(process, process.getInputStream());
			errorStream.start();
			outputStream.start();
			process.waitFor();
			System.out.println("process 执行完毕");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	class StreamManager extends Thread {
		Process process;
		InputStream inputStream;

		public StreamManager(Process process, InputStream inputStream) {
			this.process = process;
			this.inputStream = inputStream;
		}

		public void run() {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String line = null;
			try {
				while ((line = bufferedReader.readLine()) != null) {
					System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			process.destroy();
		}
	}
}