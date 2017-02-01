package com.wosloveslife.fantasy;

import com.wosloveslife.fantasy.bean.BLyric;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.wosloveslife.fantasy.manager.MusicManager.string2Int;
import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    public static final String LRC = "[00:00.79]一生为你感动\n" +
            "[00:02.48]\n" +
            "[00:04.53]作词：祁隆\n" +
            "[00:06.58]作曲：祁隆\n" +
            "[00:08.63]演唱：祁隆\n" +
            "[00:10.66]\n" +
            "[00:33.41]天天的等 \n" +
            "[00:35.44]夜夜的梦\n" +
            "[00:37.48]幻想着见到你是怎样的激动\n" +
            "[00:40.97]\n" +
            "[00:41.54]你就像是\n" +
            "[00:43.59]心灵的灯\n" +
            "[00:45.62]照亮我人生一段崭新的旅程\n" +
            "[00:48.93]\n" +
            "[00:49.79]我亲爱的\n" +
            "[00:51.74]我的宝贝\n" +
            "[00:53.77]你是我心底那块剔透的水晶\n" +
            "[00:57.05]\n" +
            "[00:57.82]一想起你\n" +
            "[00:59.85]心跳怦怦\n" +
            "[01:01.88]简直无法表达我爱你的心声\n" +
            "[01:05.57]\n" +
            "[01:08.80]我一生为你感动\n" +
            "[01:10.69]一生为你心动\n" +
            "[01:12.77]宝贝我盼了很久\n" +
            "[01:14.81]为你发了疯\n" +
            "[01:16.73]\n" +
            "[01:16.75]我一生为你感动\n" +
            "[01:18.79]一生为你心动\n" +
            "[01:20.88]你就是我的全部\n" +
            "[01:22.88]我终于圆了梦\n" +
            "[01:24.85]\n" +
            "[01:41.58]我亲爱的\n" +
            "[01:43.55]我的宝贝\n" +
            "[01:45.63]你是我心底那块剔透的水晶\n" +
            "[01:48.83]\n" +
            "[01:49.70]一想起你\n" +
            "[01:51.73]心跳怦怦\n" +
            "[01:53.71]简直无法表达我爱你的心声\n" +
            "[01:58.51]\n" +
            "[02:01.42]我一生为你感动\n" +
            "[02:03.64]一生为你心动\n" +
            "[02:05.62]宝贝我盼了很久\n" +
            "[02:07.68]为你发了疯\n" +
            "[02:09.07]\n" +
            "[02:09.57]我一生为你感动\n" +
            "[02:11.67]一生为你心动\n" +
            "[02:13.70]你就是我的全部\n" +
            "[02:15.72]我终于圆了梦\n" +
            "[02:17.49]\n" +
            "[02:34.00]我一生为你感动\n" +
            "[02:36.09]一生为你心动\n" +
            "[02:38.15]宝贝我盼了很久\n" +
            "[02:40.21]为你发了疯\n" +
            "[02:41.77]\n" +
            "[02:42.06]我一生为你感动\n" +
            "[02:44.17]一生为你心动\n" +
            "[02:46.30]你就是我的全部\n" +
            "[02:48.29]我终于圆了梦\n" +
            "[02:50.16]\n" +
            "[02:50.24]我一生为你感动\n" +
            "[02:52.34]一生为你心动\n" +
            "[02:54.45]宝贝我盼了很久\n" +
            "[02:56.51]为你发了疯\n" +
            "[02:58.03]\n" +
            "[02:58.32]我一生为你感动\n" +
            "[03:00.50]一生为你心动\n" +
            "[03:02.51]你就是我的全部\n" +
            "[03:04.56]我终于圆了梦\n" +
            "[03:06.64]\n" +
            "[03:18.87]你就是我的全部\n" +
            "[03:20.88]我终于圆了梦\n" +
            "[03:22.62]";

    @Test
    public void test() {
        List<BLyric.LyricLine> lrcLines = new ArrayList<>();
        int startPoint = 0;
        int leftIndex;
        int rightIndex;
        String lrc = LRC.replaceAll("\\n", "");
        while ((leftIndex = lrc.indexOf("[", startPoint)) != -1) {
            rightIndex = lrc.indexOf("]", ++startPoint);
            if (rightIndex == -1) continue;
            String time = lrc.substring(leftIndex + 1, rightIndex);
            int timestamp = lrcTime2Timestamp(time);

            startPoint = rightIndex + 1;
            if (startPoint >= lrc.length()) break;

            int i = lrc.indexOf("[", startPoint);
            String content = lrc.substring(startPoint, i);

            lrcLines.add(new BLyric.LyricLine(timestamp, content));
        }

        System.out.println("lrcLines = " + lrcLines.toString());
        assertEquals(lrcLines.size(), 0);
    }

    private static int lrcTime2Timestamp(String time) {
        int minutes = string2Int(time.substring(0, 2));
        int seconds = string2Int(time.substring(3, 5));
        int milliseconds = string2Int(time.substring(6, 8));
        return minutes * 60 * 1000 + seconds * 1000 + milliseconds * 10;
    }
}