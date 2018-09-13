package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuPointCloud;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class PlyReaderTest {

    private void simplePlyHeaderTest(PlyReader.PlyHeader header) {
        assertTrue(header.getPlyFormat() == PlyReader.FORMAT_ASCII);
        assertEquals(header.getPlyVersion(), 1.0f, 1e-5);
        assertTrue(header.getElementTypes().get("vertex").getPropertiesName().length == 3);
        assertTrue(header.getElementTypes().get("vertex").getPropertiesType().length == 3);
        assertTrue(header.getElementTypes().get("face").getListType1() == PlyReader.TYPE_UCHAR);
        assertTrue(header.getElementTypes().get("face").getListType2() == PlyReader.TYPE_INT);
        for (Pair<String, Integer> pair : header.getElementsNumber()) {
            if (pair.getKey().equals("vertex")) {
                assertTrue(pair.getValue() == 4770);
            } else if (pair.getKey().equals("face")) {
                assertTrue(pair.getValue() == 0);
            }
        }
    }

    @Test
    public void readAsciiPlyHeaderTest() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("pc/ply/simple.ply").getFile());
        PlyReader.PlyHeader header = reader.readHeaderThenCloseFile(file);
        simplePlyHeaderTest(header);
    }

    @Test
    public void readAsciiPlyDataTest() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("pc/ply/simple.ply").getFile());
        PcuPointCloud pointCloud = new PcuPointCloud();
        reader.readPointCloud(file, pointCloud, new ReadListener<PcuPointCloud>() {
            @Override
            public void onSucceed(PcuPointCloud pointCloud, PlyReader.PlyHeader header) {
                simplePlyHeaderTest(header);
                assertTrue(pointCloud.getPoint3ds().size() == 4770);
                assertTrue(pointCloud.getPoint3ds().get(1000).length == 3);
            }

            @Override
            public void onFail(int code, String message) {
                Assert.assertTrue(false);
            }
        });
    }

    @Test
    public void readBinaryBigEndianPlyDataTest() {

    }

    @Test
    public void readBinaryLittleEndianPlyDataTest() {
        PlyReader reader = new PlyReader();
//        File file = new File("D:\\User\\zhouh\\Documents\\CQU_SKELETON\\Data\\收集数据\\surface-L2-clean1.ply");
//        PcuPointCloud pointCloud = new PcuPointCloud();
//        reader.readPointCloud(file, pointCloud, new ReadListener<PcuPointCloud>() {
//            @Override
//            public void onSucceed(PcuPointCloud pointCloud, PlyReader.PlyHeader header) {
//                System.out.println("????");
//            }
//
//            @Override
//            public void onFail(int code, String message) {
//                System.out.println(message);
//            }
//        });
    }
}