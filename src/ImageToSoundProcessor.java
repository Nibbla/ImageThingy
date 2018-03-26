import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Niebisch Markus on 13.03.2018.
 */
public class ImageToSoundProcessor implements Visualizable {
    private final JLabel label;
    private JPanel panel;
    private JFrame paintWindow;
    private PaintMode pm;
    private String[] taskNaration = new String[PaintMode.values().length];
    private BufferedImage[] images = new BufferedImage[PaintMode.values().length];
    private final double maxVolume = 127;
    private final double maxValue = 256 * 256 * 0.5;
    private final double doublePi = 2* Math.PI;
    private double[] offsets;

    // private BufferedImage bi1_2;

    private enum PaintMode{
        task1_1,task1_2,task1_3, task1_31,task1_32,task1_33,task1_34,task1_35,contrastSketching,clipping,rangeCompression,task1_51,task1_52,task1_53,task1_54,task1_55,task1_histogramm,task2_6,task2_71,task2_72,task2_73,task3, taskGradMagnit0, taskGradMagnit1, taskGradMagnit2, taskGradMagnit3, taskGradMagnit4, askUser1, askUser2, askUser3 , zeroCross;

        private BufferedImage image;
        private String naration;

        public void setImage(BufferedImage image) {
            this.image = image;
        }

        public void setNaration(String nar) {
            this.naration=nar;
        }

        public BufferedImage getImage() {
            return image;
        }
    }
    static final Logger logger = Logger.getLogger(ImageToSoundProcessor.class.getName());




    private PaintMode paintMode = PaintMode.task1_1;


    public ImageToSoundProcessor(String s, String s1, String s2){


        task1_1(s,PaintMode.task1_1);
        initImages(images[PaintMode.task1_1.ordinal()]); // has to after task1_1 and before loading of the other images
        task1_1(s1,PaintMode.task1_2);
        task1_1(s2,PaintMode.task1_3);


       // images[PaintMode.task2_6.ordinal()] = Utils.loadBufferedImage(s1);;
       // images[PaintMode.task3.ordinal()] = Utils.loadBufferedImage(s2);
        //bi3 = loadBufferedImage(s2);
        initNaration();
        byte[] b = randomByte(1234000);
        int sampleRate = 0XAC44; //44100hz
        int byteRate = 0X15888; //2*44100hz=15888

     //   int[] sin = sinus(4,10000,sampleRate,0.3);

       int[] val =  convertImageToSound(PaintMode.task1_1,8,sampleRate,0,22000);
        save(val,"test2.wav",sampleRate,byteRate);
      //  task1_2();
      //  task1_3();
      //  task1_4( images[PaintMode.task1_2.ordinal()]);
      //  task1_5( images[PaintMode.task1_2.ordinal()]);

     //   task2( images[PaintMode.task2_6.ordinal()]);
      //  task3( images[PaintMode.task3.ordinal()]);
        //https://en.wikipedia.org/wiki/Histogram_equalization#Implementation
        label = new JLabel("Hallo");
        
        
        
        this.setPaintWindow(this.createPaintWindow(this));

       


        this.paintWindow.setLayout(new BorderLayout());
        this.paintWindow.add(panel,BorderLayout.CENTER);
        this.paintWindow.add(label,BorderLayout.SOUTH);
        this.setWindowSize();

        this.getPaintWindow().addKeyListener(createKeyListener(getPaintWindow()));
        this.getPaintWindow().addMouseListener(createMouseListener(getPaintWindow()));
    }

    private double convert(int x, int y, int w, int h, int grayVal, int maxGrayVal , double intensity, double grouping, double[] precalc, double sampleRate){
        double maxV = (maxGrayVal-grayVal*1.) / maxGrayVal;
        double[] offsets = getOffsets(h,w,sampleRate);
        double i = x + offsets[(int) (y)];
        double pre = precalc[y];

        double val = (Math.sin(i * pre)) * maxValue* maxV* intensity / grouping;
        return val;
    }

    private void createOffsets(double[] multfactor, int h) {

            offsets = new double[h];
            Random r = new Random();
            double offsetPerStep = doublePi /h + Math.PI;
            double sum = 0;
            for (int i = 0; i < h; i++) {
               // offsets[i] = (offsetPerStep * i) % doublePi - Math.PI;
               offsets[i] = Math.PI * (r.nextDouble()-0.5);
                // offsets[i] = doublePi*(r.nextDouble())-Math.PI;
                sum += offsets[i];
            }
            sum = sum;

    }
    private double[] getOffsets(int h, int w, double sampleRate) {

        return offsets;
    }

    private int[] sinus(int lengthInSecond, int herz, int sampleRate, double intensity) {
        Random r = new Random();
        r.nextInt(65536);
        int[] value = new int[lengthInSecond*sampleRate];
        double maxValue = 256 * 256 * 0.5*intensity;
        double dpi = 2*Math.PI;
        int count = 0;
        for (double i = 0; i < value.length; i++) {
            int val = (int) (Math.sin(dpi*i * herz / sampleRate) * maxValue);
            value[count++] = val;
        }
        return value;
    }

    private int[] convertImageToSound(PaintMode paintMode, int lengthInSecond, int sampleRate, int minHerz, int maxHerz) {
        BufferedImage b1 = paintMode.getImage();
        BufferedImage grayScale = transFormToGrayScale(b1);
        int w = grayScale.getWidth();
        int h = grayScale.getHeight();
        byte[][] grayScaleB = new byte[grayScale.getWidth()][grayScale.getHeight()];
        int numSamples = lengthInSecond*sampleRate;
        double[] value = new double[numSamples];

        int samplePerX = numSamples / w;


        double[] herzValues = new double[h];
        double[] precalcMultiPlicationFactor = new double[h];

        for (int y = 0; y < h; y++) {
            herzValues[y] =( 1.- (y*1./h)) * (maxHerz-minHerz)+ minHerz;
            precalcMultiPlicationFactor[y] = doublePi*herzValues[y] / sampleRate;
        }
        createOffsets(herzValues,h);
        for (int x = 0; x < w; x++) {
            int sampleX = x * samplePerX;
            createOffsets(precalcMultiPlicationFactor,h);
            for (int dx = 0; dx < samplePerX; dx++) {

            int[] grayScaleAtXY = new int[h];
            double ySum = 0;
            for (int y = 0; y < h; y++) {
                grayScaleAtXY[y] = new Color(grayScale.getRGB(x, y)).getRed();

            }
            for (int y = 0; y < h; y++) {

                    double valueAtX = convert(x+dx, y, w, h, grayScaleAtXY[y], 256 , 1, h,precalcMultiPlicationFactor, sampleRate);
                    value[sampleX+dx] += valueAtX;


            }

            }

        }
        int[] intvalue = new int[numSamples];
        for (int i = 0; i < numSamples; i++) {
            intvalue[i] = (int) value[i];
        }
        return intvalue;
    }



    private byte[] randomByte(int i) {
        byte[] b = new byte[i];
        Random r = new Random();
        r.nextBytes(b);

       return b;
    }



    public void save(int[] data, String path, int sampleRate, int byteRate){
        int format = 1;
        int channels = 1;


        int bitsPerSample = 0X10;
      //  int blockAlign = 2;
        int blockAlign = (bitsPerSample+7)/8;
        blockAlign = 2;
        try (FileOutputStream fos = new FileOutputStream(path)) {


            fos.write(encodeToByteArray("RIFF"), 0, 4); //offset: 0
            fos.write(intToByteLittleEndian(data.length*2+36), 0, 4); // full datalenght + header - 8 //offset: 4
           // fos.write(BitConverter.GetBytes(CInt((Me.myDataSize + &H24))), 0, 4);
            fos.write(encodeToByteArray("WAVE"), 0, 4); //offset: 8
            fos.write(encodeToByteArray("fmt "), 0, 4); //offset: 12
            fos.write(intToByteLittleEndian(16), 0, 4); //offset: 16
            fos.write(intToByteLittleEndian(format), 0, 2); //offset: 20
            fos.write(intToByteLittleEndian(channels), 0, 2); //offset: 22
            fos.write(intToByteLittleEndian(sampleRate), 0, 4); //offset: 24
            fos.write(intToByteLittleEndian(byteRate), 0, 4); //offset: 28
            fos.write(intToByteLittleEndian(blockAlign), 0, 2); //offset: 32
            fos.write(intToByteLittleEndian(bitsPerSample), 0, 2); //offset: 34
            fos.write(encodeToByteArray("data"), 0, 4);
            fos.write(intToByteLittleEndian(data.length*2-44), 0, 4);
            //Dim stream2 As New FileStream(Me.myPath, FileMode.Create)
            //stream2.Write(buffer, 0, buffer.Length)
            for (int i = 0; i < data.length; i++) {
                fos.write(intToByteLittleEndian(data[i]), 0,2);
            }



            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] encodeToByteArray(String ste) {
        ByteBuffer lsdkfj = Charset.forName("UTF-8").encode(ste);
        return lsdkfj.array();
    }

    /*
    Public Sub Save()
    Dim buffer As Byte() = New Byte(&H2C  - 1) {}
    Dim stream As New MemoryStream(buffer, 0, &H2C, True)
    Dim encoding As New UTF8Encoding
    !!!!ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(myString)!!!! encoding for java
            stream.Write(encoding.GetBytes("RIFF"), 0, 4)
                    stream.Write(BitConverter.GetBytes(CInt((Me.myDataSize + &H24))), 0, 4)
            stream.Write(encoding.GetBytes("WAVE"), 0, 4)
            stream.Write(encoding.GetBytes("fmt "), 0, 4)
            stream.Write(BitConverter.GetBytes(&H10), 0, 4)
            stream.Write(BitConverter.GetBytes(Me.myFormat), 0, 2)
            stream.Write(BitConverter.GetBytes(Me.myChannels), 0, 2)
            stream.Write(BitConverter.GetBytes(Me.mySampleRate), 0, 4)
            stream.Write(BitConverter.GetBytes(Me.myByteRate), 0, 4)
            stream.Write(BitConverter.GetBytes(Me.myBlockAlign), 0, 2)
            stream.Write(BitConverter.GetBytes(Me.myBitsPerSample), 0, 2)
            stream.Write(encoding.GetBytes("data"), 0, 4)
            stream.Write(BitConverter.GetBytes(Me.myDataSize), 0, 4)
    Dim stream2 As New FileStream(Me.myPath, FileMode.Create)
            stream2.Write(buffer, 0, buffer.Length)
            stream2.Write(Me.myData, 0, Me.myData.Length)
    stream2.Close
    End Sub
    */

    ByteBuffer _intShifter2 = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE)
            .order(ByteOrder.BIG_ENDIAN);

    public byte[] intToByteBigEndian(int value) {
        _intShifter2.clear();
        _intShifter2.putInt(value);
        return _intShifter2.array();
    }

    public int byteToIntBigEndian(byte[] data)
    {
        _intShifter2.clear();
        _intShifter2.put(data, 0, Integer.SIZE / Byte.SIZE);
        _intShifter2.flip();
        return _intShifter2.getInt();
    }

    ByteBuffer _intShifter = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE)
            .order(ByteOrder.LITTLE_ENDIAN);

    public byte[] intToByteLittleEndian(int value) {
        _intShifter.clear();
        _intShifter.putInt(value);
        return _intShifter.array();
    }

    public int byteToIntLittleEndian(byte[] data)
    {
        _intShifter.clear();
        _intShifter.put(data, 0, Integer.SIZE / Byte.SIZE);
        _intShifter.flip();
        return _intShifter.getInt();
    }
    /*
    Private Function createSound(ByVal tonInHerz As Integer, ByVal lautstärkeInProzent As Double, ByVal längeInSekunden As Integer) As WavFileForIO
            Dim rio2 As New WavFileForIO
            Dim num3 As Double = ((127 * lautstärkeInProzent) * 0.01)
            Dim a As Byte() = New Byte((((längeInSekunden * Me.wavFile.ByteRate) - 1) + 1)  - 1) {}
            Dim num As Double = (CDbl(Me.wavFile.SampleRate) / CDbl(tonInHerz))
            Dim num2 As Double = (VBMath.Rnd * num)
            a(0) = 1
            a(1) = 0
            Dim num9 As Integer = (a.Length - 1)
            Dim i As Integer = 2
            Do While (i <= num9)
                Dim twobyte As New twobyte
                Dim num6 As Double = 0
                Dim num5 As Double = ((num2 / num) - (CLng(Math.Round(num2)) / CLng(Math.Round(num))))
                num6 = (Math.Cos(((num5 * 2) * 3.1415926535897931)) * num3)
                num6 = (256 + num6)
                twobyte.setAusschlag(CUShort(Math.Round(num6)))
                Dim num7 As Byte = twobyte.getUpByte
                Dim num8 As Byte = twobyte.getLowByte
                a(i) = twobyte.getUpByte
                a((i + 1)) = twobyte.getLowByte
                num2 += 1
                i = (i + 2)
            Loop
            a = Me.getDifferention(a)
            rio2.Data = a
            Return rio2
        End Function
     */
    /*
    Private Function createSounds(ByVal sounds As mTon()) As WavFileForIO
            Dim num2 As Double
            Dim endeInSekunden As Double
            Dim num6 As Double
            Dim num7 As Double
            Dim num10 As Integer
            Dim rio2 As New WavFileForIO
            Dim num8 As Double = 65536
            Dim a As Double = (VBMath.Rnd * num2)
            Dim ton As mTon
            For Each ton In sounds
                If (ton.EndeInSekunden > endeInSekunden) Then
                    endeInSekunden = ton.EndeInSekunden
                End If
            Next
            Dim num As Integer = CInt(Math.Round(CDbl(((endeInSekunden * Me.wavFile.SampleRate) - Me.modulo((endeInSekunden * Me.wavFile.SampleRate), 4)))))
            Dim numArray As Double() = New Double(((num - 1) + 1)  - 1) {}
            Dim ton2 As mTon
            For Each ton2 In sounds
                num2 = (CDbl(rio2.SampleRate) / CDbl(ton2.herz))
                a = (VBMath.Rnd * num2)
                Dim num4 As Double = ((ton2.lautstärke / 100) * 127)
                Dim num12 As Integer = CInt(Math.Round(CDbl((ton2.startInSekunden * rio2.SampleRate))))
                Dim num11 As Integer = CInt(Math.Round(CDbl((((ton2.startInSekunden + ton2.LängeInSekunden) * rio2.SampleRate) - 1))))
                Dim num18 As Integer = (num11 - 1)
                num10 = num12
                Do While (num10 <= num18)
                    Dim num14 As Double
                    Dim num13 As Double = ((a / num2) - (CLng(Math.Round(a)) / CLng(Math.Round(num2))))
                    If (ton2.herz = -1) Then
                        num14 = (VBMath.Rnd * num4)
                    Else
                        num14 = (Math.Sin(((num13 * 2) * 3.1415926535897931)) * num4)
                    End If
                    numArray(num10) = (num14 + numArray(num10))
                    a += 1
                    num10 += 1
                Loop
            Next
            Dim numArray2 As Double() = DirectCast(numArray.Clone, Double())
            Dim num15 As Double
            For Each num15 In numArray
                If (num15 > num7) Then
                    num7 = num15
                End If
                If (num15 < num8) Then
                    num8 = num15
                End If
            Next
            If (Math.Abs(num7) > Math.Abs(num8)) Then
                num6 = Math.Abs(num7)
            Else
                num6 = Math.Abs(num8)
            End If
            Dim num9 As Double = 1
            num9 = ((CLng(Math.Round(num6)) / &H7F) + 1)
            Dim buffer As Byte() = New Byte((CInt(Math.Round(CDbl(((CDbl((numArray.Length * rio2.BitsPerSample)) / 8) - 1)))) + 1)  - 1) {}
            Dim num20 As Integer = (numArray.Length - 1)
            num10 = 0
            Do While (num10 <= num20)
                Dim twobyte As New twobyte
                twobyte.setAusschlag(CUShort(Math.Round(CDbl(((numArray(num10) / num9) + 256)))))
                buffer((num10 * 2)) = twobyte.getUpByte
                buffer(((num10 * 2) + 1)) = twobyte.getLowByte
                num10 += 1
            Loop
            buffer(0) = 1
            buffer(1) = 0
            buffer = Me.getDifferention(buffer)
            rio2.Data = buffer
            Return rio2
        End Function
     */

    /*
     Private Function getDifferention(ByVal a As Byte()) As Byte()
            Dim twobyteArray2 As twobyte() = Me.ByteArrayInTwoByteArray(a)
            Dim twobyteArray As twobyte() = New twobyte(((twobyteArray2.Length - 1) + 1)  - 1) {}
            Dim num4 As Integer = twobyteArray2(0).getAusschlag
            twobyteArray(0) = New twobyte
            twobyteArray(0).setAusschlag(&H100)
            Dim num3 As Integer = CInt(Math.Round(CDbl((127 + num4))))
            Dim num2 As Integer = CInt(Math.Round(CDbl((-127 + num4))))
            Dim num9 As Integer = (twobyteArray2.Length - 1)
            Dim i As Integer = 1
            Do While (i <= num9)
                Dim num8 As Integer
                If (i = &HA4) Then
                    i = &HA4
                End If
                Dim num7 As Integer = twobyteArray2(i).getAusschlag
                Dim num6 As Integer = twobyteArray2((i - 1)).getAusschlag
                Dim num As Integer = (num7 - num6)
                Dim twobyte As New twobyte
                If (((twobyteArray2((i - 1)).getAusschlag + num) - num4) >= (num3 - num4)) Then
                    num8 = num3
                ElseIf (((twobyteArray2((i - 1)).getAusschlag + num) - num4) <= (num2 - num4)) Then
                    num8 = num2
                Else
                    num8 = CInt(Math.Round(CDbl((((twobyteArray2((i - 1)).getAusschlag + num) - num4) + 256))))
                End If
                twobyte.setAusschlag(CUShort(num8))
                twobyteArray(i) = twobyte
                i += 1
            Loop
            Return Me.TwoByteArrayInByteArray(twobyteArray)
        End Function
     */

    private void task3(BufferedImage image) {
        BufferedImage bi1 =  image;
        int w = bi1.getWidth();
        int h = bi1.getHeight();

        BufferedImage grayScale = transFormToGrayScale(bi1);
        double[][] filter = createSumFilter(1.,9,1);
        double[] gradMagnitudeThreshold = {0.05,0.25,0.5,0.75,1};
        gradMagnitude(grayScale,gradMagnitudeThreshold, PaintMode.taskGradMagnit0, "");


        double[][][] askUser1 = askUserForFilter(grayScale,3, FilterType.Sobel, FilterOrientation.Vertical);
        BufferedImage img = Utils.copyImage(grayScale);
        for (int i = 0; i < askUser1.length; i++) {
            img = task2_6(img, -1, askUser1[i], PaintMode.askUser1, "");
        }

        double[][][] askUser2 = askUserForFilter(grayScale,4, FilterType.Prewitt, FilterOrientation.Horicental);
        img = Utils.copyImage(grayScale);
        for (int i = 0; i < askUser2.length; i++) {
            img = task2_6(img,-1,askUser2[i], PaintMode.askUser2,"");
        }
        img = Utils.copyImage(grayScale);
        double[][][] askUser3 = askUserForFilter(grayScale,5, FilterType.Prewitt, FilterOrientation.Both);
        for (int i = 0; i < askUser3.length; i++) {
            task2_6(img,-1,askUser3[i], PaintMode.askUser3,"");
        }

        img = Utils.copyImage(grayScale);
        double[][] laplace = create3by3SimpleLaplace();

        taskZeroCrossing(img,laplace, PaintMode.zeroCross,"");


    }

    private BufferedImage taskZeroCrossing(BufferedImage grayScale, double[][] filter, PaintMode paintMode, String comment) {
        int w = grayScale.getWidth();
        int h = grayScale.getHeight();
        int[] convolutedValues = new int[w];

        BufferedImage imageCorrected = Utils.copyImage(grayScale);
        BufferedImage imageZero = Utils.copyImageHull(grayScale);
        //something something
        int numberPixels = w*h;



        Color c;
        Color n;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {

                c = new Color(grayScale.getRGB(x,y));
                double newVal = 0;
                int xval = (c.getBlue());

                int dimX = filter.length;
                int dimY = filter[0].length;
                int leftBound = 0-dimX/2;
                int topBound = 0-dimY/2;
                int rightBound = 0+dimX/2;
                int bottomBound = 0+dimY/2;
                for (int dx = leftBound; dx <= rightBound; dx++) {
                    for (int dy = topBound; dy <= bottomBound; dy++) {
                        int lookupX = dx+x;
                        int lookupY = dy+y;
                        double multValue = filter[dx - leftBound][dy - topBound];
                        if (lookupX < 0) lookupX = 1;
                        if (lookupY < 0) lookupY = 1;
                        if (lookupX >= w) lookupX = w-2;
                        if (lookupY >= h) lookupY = h-2;


                        Color c1 = new Color(grayScale.getRGB(lookupX,lookupY));

                        int val = (c1.getBlue());


                        newVal += multValue * val;

                    }
                }
                //try{
                int newValInt = (int) newVal;
                // System.out.println(newValInt);
                Color newC;
                if (newValInt<0) newC = Color.BLACK;
                else newC = Color.WHITE;

                imageCorrected.setRGB(x,y,newC.getRGB());
                if (x == 0||y==0 ) imageZero.setRGB(x,y,Color.black.getRGB()); else{
                    if (imageCorrected.getRGB(x-1,y) != imageCorrected.getRGB(x,y)||imageCorrected.getRGB(x,y-1) != imageCorrected.getRGB(x,y)){
                        imageZero.setRGB(x,y,Color.WHITE.getRGB());} else{ imageZero.setRGB(x,y,Color.BLACK.getRGB());}
                    }
                }

            }







        //BufferedImage concat = Utils.concatImagesHorizontal(grayScale, imageCorrected);
        BufferedImage concat2 = Utils.concatImagesHorizontal(imageCorrected, imageZero);
       // BufferedImage concat3 = Utils.concatImagesVertical(concat,concat2);

        images[paintMode.ordinal()] = concat2;
        taskNaration[paintMode.ordinal()] = comment + "\nZeroCrosses?\nleft is the laplaceFilter with negative sign being black and positive sign being white\nright is" +
                " black if no change occured and white if it did\nWidth: " + w + "\nHeight: " + h + "\n";
        return imageCorrected;
    }

    private double[][] create3by3SimpleLaplace() {
        double[][] l = new double[3][3];
        l[0][0] = 0;
        l[0][1] = 1;
        l[0][2]= 0;

        l[1][0]= 1;
        l[1][1]= -4;
        l[1][2]= 1;

        l[2][0]= 0;
        l[2][1]= 1;
        l[2][2]= 0;
        return l;
    }

    private double[][][] askUserForFilter(BufferedImage someImage, int threshold, FilterType filterType, FilterOrientation filterOrientation) {

        int[] selections = multipleSelectionInputBox(someImage,threshold,filterType,filterOrientation, "Select Values for Edge Detection\nThreshold only applys for Sobel");

        threshold = selections[0];
        filterType = FilterType.values()[selections[1]];
        filterOrientation = FilterOrientation.values()[selections[2]];
        double[][][] filter = new double[1][][];// = new double[2][][];
        if (filterType == FilterType.Prewitt) threshold = -1;
        switch (filterOrientation){

            case Vertical:
                filter = new double[1][][];
                filter[0] = createEdgeFilter(threshold,filterOrientation);
                break;
            case Horicental:
                filter = new double[1][][];
                filter[0] = createEdgeFilter(threshold,filterOrientation);
                break;
            case Both:
                filter = new double[2][][];

                filter[0] = createEdgeFilter(threshold, FilterOrientation.Horicental);
                filter[1] = createEdgeFilter(threshold,filterOrientation.Vertical);

                break;
        }



        return filter;
    }

    private double[][] createEdgeFilter(int threshold, FilterOrientation filterOrientation) {

        double[][] filter = new double[3][3];
        int signThreshold = (int) Math.signum(threshold);
        switch (filterOrientation) {
            case Horicental:
                filter[0][0] = signThreshold;
                filter[0][1] = threshold;
                filter[0][2] = signThreshold;
                 filter[1][0] = 0;
                 filter[1][1] =0;
                 filter[1][2] =0;
                 filter[2][2] = -signThreshold;
                 filter[2][1] = -threshold;
                 filter[2][2] = -signThreshold;
                break;
            case Vertical:
                filter[0][0] = signThreshold;
                filter[0][1] = 0;
                filter[0][2] = -signThreshold;
                filter[1][0] = threshold;
                filter[1][1] =0;
                filter[1][2] =-threshold;
                filter[2][2] = signThreshold;
                filter[2][1] = 0;
                filter[2][2] = -signThreshold;
                break;
            case Both:
                break;
        }


        return filter;
    }

    private int[] multipleSelectionInputBox(BufferedImage someImage, int defThreshold, FilterType defFilterType, FilterOrientation defFilterOrientation, String comment){
        int[] resultvalue = new int[1+ FilterType.values().length+ FilterOrientation.values().length];
       // JTextField[] field = new JTextField[resultvalue];

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel,BoxLayout.Y_AXIS));
        if (someImage!=null) {

            ImageIcon image = new ImageIcon(someImage);
            JLabel lbl = new JLabel(image);
            myPanel.add(lbl);
        }

        String[] filterType = new String[FilterType.values().length];
        for (int i = 0; i < filterType.length; i++) filterType[i] = FilterType.values()[i].name();
        String[] filterOrientation = new String[FilterOrientation.values().length];
        for (int i = 0; i < filterOrientation.length; i++) filterOrientation[i] = FilterOrientation.values()[i].name();

        JComboBox typeList = new JComboBox(filterType);
        typeList.setSelectedIndex(defFilterType.ordinal());
        JComboBox orientList = new JComboBox(filterOrientation);
        orientList.setSelectedIndex(defFilterOrientation.ordinal());
        JPanel line = new JPanel();
        line.setLayout(new BoxLayout(line,BoxLayout.X_AXIS));
        JTextField tf = new JTextField(5);
        line.add(new JLabel("Threshold: "));
        line.add(tf);
        tf.setText(String.valueOf(defThreshold));
        myPanel.add(line);

        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        myPanel.add(typeList);

        myPanel.add(Box.createHorizontalStrut(15)); // a spacer
        myPanel.add(orientList);



        myPanel.add(new JLabel(Utils.textConvertForJLabel(comment)));

        int result = JOptionPane.showConfirmDialog(null, myPanel,
                "Please select Values", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            resultvalue[0] =  Integer.parseInt(tf.getText());
            resultvalue[1] =  typeList.getSelectedIndex();
            resultvalue[2] =  orientList.getSelectedIndex();


        }else resultvalue = null;


        return resultvalue;
    }

    private enum FilterType{
        Sobel,Prewitt;
    }
    private enum FilterOrientation{
        Vertical,Horicental,Both;
    }

    private void gradMagnitude(BufferedImage grayScale, double[] threshold, PaintMode paintMode, String s) {
        BufferedImage[] newImg = new BufferedImage[threshold.length];
        Graphics[] graphics = new Graphics[threshold.length];
        for (int i = 0; i < threshold.length; i++) {
            newImg[i] = Utils.copyImageHull(grayScale);

            graphics[i] = newImg[i].getGraphics();
        }

        int w = grayScale.getWidth();
        int h = grayScale.getHeight();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                double magnit;
                int xLeft = x - 1;
                if (xLeft<0) xLeft = x;
                int xRight = x + 1;
                if (xRight>=w) xRight = x;
                int xValLeft = new Color(grayScale.getRGB(xLeft,y)).getBlue();
                int xValRight = new Color(grayScale.getRGB(xRight,y)).getBlue();
                double gradX = (xValRight-xValLeft)/(xRight-xLeft);


                int yTop = y - 1;
                if (yTop<0) yTop = y;
                int yBottom = y + 1;
                if (yBottom>=h) yBottom = y;
                int yValTop = new Color(grayScale.getRGB(x,yTop)).getBlue();
                int yValBottom = new Color(grayScale.getRGB(x,yBottom)).getBlue();
                double gradY = (yValBottom-yValTop)/(yBottom-yTop);

                magnit = Math.sqrt(gradX*gradX+gradY*gradY);

                for (int i = 0; i < threshold.length; i++) {
                    double singleThreshold = threshold[i]*255;
                    Color c = Color.BLACK;
                    if (magnit<0)System.out.println("neg");
                    if (magnit>=singleThreshold) c = Color.WHITE;
                    graphics[i].setColor(c);
                    graphics[i].drawRect(x,y,1,1);
                }

            }
        }

        for (int i = 0; i < threshold.length; i++) {
            images[paintMode.ordinal()+i] = newImg[i];
            taskNaration[paintMode.ordinal()+i] = "https://en.wikipedia.org/wiki/Image_gradient#Math used\nGradient calculated by Sqrt((deltaXValue/deltaXDistance)^2+(deltaYValue/deltaYDistance)^2) of neighbours to center\nGradient Magnitude threshold: " + threshold[i] + "\n" + s;
        }


    }

    private void task2(BufferedImage image) {
        BufferedImage bi1 =  image;
        int w = bi1.getWidth();
        int h = bi1.getHeight();

        BufferedImage grayScale = transFormToGrayScale(bi1);
        double[][] filter = createSumFilter(1.,9,1);
        task2_6(grayScale, 200,filter, PaintMode.task2_6, "1d 9 filter with sum 1");


        double[][] filter2 = createSumFilter(1.,3,3);
        BufferedImage someImage = task2_6(grayScale, -1, filter2, PaintMode.task2_71, "2d 3by3 filter with sum 1");

        double[][] filter3 = createSumFilter(1.,11,1);
        task2_6(someImage, -1,filter3, PaintMode.task2_72, "1d 11value filter with sum 1; took eleven because its easier to get the center\nIt makes the image go woosh");

        double[][] filterhp = createHpFilter();
        task2_6(grayScale, -1,filterhp, PaintMode.task2_73, "filter Hp; used clipping to 0/255 for negative and positive values. \nLooks like Edge detection");

        // images[PaintMode.task2_6.ordinal()] = grayScale;
        //taskNaration[PaintMode.task2_6.ordinal()] = "Gray Scaled\nWidth: " + w + "\nHeight: " + h;
    }

    private double[][] createHpFilter() {
        double[][] hp = new double[3][3];
        hp[0] = new double[]{0, -1, 0};
        hp[1] = new double[]{-1, 4, -1};
        hp[2] = new double[]{0, -1, 0};


        return hp;
    }


    private BufferedImage task2_6(BufferedImage grayScale, int lineY, double[][] filter, PaintMode paintMode, String comment) {
        int w = grayScale.getWidth();
        int h = grayScale.getHeight();
        int[] convolutedValues = new int[w];

        BufferedImage imageCorrected = Utils.copyImage(grayScale);
        //something something
        int numberPixels = w*h;



        Color c;
        Color n;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (lineY != -1 && y != lineY) continue;
                c = new Color(grayScale.getRGB(x,y));
                double newVal = 0;
                int xval = (c.getBlue());

                int dimX = filter.length;
                int dimY = filter[0].length;
                int leftBound = 0-dimX/2;
                int topBound = 0-dimY/2;
                int rightBound = 0+dimX/2;
                int bottomBound = 0+dimY/2;
                for (int dx = leftBound; dx <= rightBound; dx++) {
                    for (int dy = topBound; dy <= bottomBound; dy++) {
                        int lookupX = dx+x;
                        int lookupY = dy+y;
                        double multValue = filter[dx - leftBound][dy - topBound];
                        if (lookupX < 0) lookupX = 1;
                        if (lookupY < 0) lookupY = 1;
                        if (lookupX >= w) lookupX = w-2;
                        if (lookupY >= h) lookupY = h-2;

                        Color c1 = new Color(grayScale.getRGB(lookupX,lookupY));

                        int val = (c1.getBlue());


                        newVal += multValue * val;

                    }
                }
                //try{
                int newValInt = (int) newVal;
               // System.out.println(newValInt);
                if (newValInt<0) newValInt=0;
                if (newValInt>255) newValInt=255;
                n = new Color(newValInt,newValInt,newValInt);
                convolutedValues[x] = newValInt;
                imageCorrected.setRGB(x,y,n.getRGB());

            }

        }




        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w; i++) {
            sb.append(convolutedValues[i] + ";");
        }
        if (lineY == -1) sb = new StringBuilder();
        BufferedImage concat = Utils.concatImagesHorizontal(grayScale, imageCorrected);

        images[paintMode.ordinal()] = concat;
        taskNaration[paintMode.ordinal()] = comment + "\nGray Scaled\nWidth: " + w + "\nHeight: " + h + "\n" + sb.toString();
        return imageCorrected;
    }

    private double[][] createSumFilter(double sumOfFilter, int dimx, int dimy) {

        double[][] f = new double[dimx][dimy];
        double standart = sumOfFilter/(dimx*dimy);
        double sum = 0;
        for (int x = 0; x < dimx; x++) {
            for (int y = 0; y < dimy; y++) {
                f[x][y] = standart;// * Math.random();
                sum += f[x][y]; 
            }
        }
        //int center = dim / 2;
        //sum -= f[center][center];
        //f[center][center] = sumOfFilter - sum;
        
        return f;
    }

    private double[][] createGausfilter(double constant, int dim) {
        double[][] f = new double[dim][dim];
        double standart = constant/(dim*dim);
        double sum = 0;
        for (int x = 0; x < dim; x++) {
            for (int y = 0; y < dim; y++) {
                f[x][y] = standart * Math.random();
                sum += f[x][y];
            }
        }
        sum -= f[5][5];
        f[5][5] = constant - sum;

        return f;
    }

    private void task1_5(BufferedImage image) {
        int[] binSizes = createBinSizes(5);
        for (int i = 0; i < binSizes.length; i++) {
            createHistogram(image,binSizes[i], PaintMode.values()[PaintMode.task1_51.ordinal()+i]);
        }
        histogrammEqualization(image,32);

    }

    private void histogrammEqualization(BufferedImage image,int binSize) {
        BufferedImage imageCorrected = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
        //something something
        int w = image.getWidth();
        int h = image.getHeight();
        int numberPixels = w*h;



        BufferedImage histogramm = new BufferedImage(image.getWidth()*2,300,image.getType());
        Graphics2D g2 = histogramm.createGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0,0,histogramm.getWidth(),histogramm.getHeight());
        g2.setColor(Color.WHITE);
        g2.fillRect(3,3,histogramm.getWidth()-6,histogramm.getHeight()-6);



        double[] grayLevels = getGrayLevels(image);



        double[] probGrayLevels = new double[256];
        double[] cummulativeDistributionFunction = new double[256];
        createProbGrayLevelsAndCummulativeDistributionFunction(numberPixels, probGrayLevels, cummulativeDistributionFunction, grayLevels);







        Color c;
        Color n;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                c = new Color(image.getRGB(x,y));
                int newVal = 0;
                int xval = (c.getBlue());

                newVal = (int) (cummulativeDistributionFunction[xval]*255);
                //try{
                try {
                    n = new Color(newVal,newVal,newVal);
                    imageCorrected.setRGB(x,y,n.getRGB());
                }catch (Exception e){
                    e.printStackTrace();
                }



            }

        }

        double[] grayLevels2 = getGrayLevels(imageCorrected);
        double bin[] = new double[binSize];
        double binNormalized[] = new double[binSize];
        fillBins(grayLevels2,bin,numberPixels,binNormalized);


        g2.setColor(Color.BLUE);
        double binWidth = (histogramm.getWidth()-6)/(binSize*1.);
        double cant = 1/maximumOfArray(binNormalized);
        for (int i = 0; i < binSize; i++) {

            int height = (int) (binNormalized[i]*cant*histogramm.getHeight()-6);
            g2.fillRect((int)(i*binWidth+3),(int) ((histogramm.getHeight()-3)-height), (int) binWidth,height );
        }



        BufferedImage both = Utils.concatImagesHorizontal(image,imageCorrected);
        BufferedImage all = Utils.concatImagesVertical(both,histogramm);

        images[PaintMode.task1_histogramm.ordinal()] = all;
        taskNaration[PaintMode.task1_histogramm.ordinal()] = "Histogramm:\nBins: " + binSize + "\nCant of Histograph: " + cant;
    }

    private void createHistogram(BufferedImage image, int binSize, PaintMode paintMode) {
        BufferedImage imageCorrected = new BufferedImage(image.getWidth(),image.getHeight(),image.getType());
        //something something
        int w = image.getWidth();
        int h = image.getHeight();
        int numberPixels = w*h;



        BufferedImage histogramm = new BufferedImage(image.getWidth()*2,300,image.getType());
        Graphics2D g2 = histogramm.createGraphics();
        g2.setColor(Color.BLACK);
        g2.fillRect(0,0,histogramm.getWidth(),histogramm.getHeight());
        g2.setColor(Color.WHITE);
        g2.fillRect(3,3,histogramm.getWidth()-6,histogramm.getHeight()-6);



        double[] grayLevels = getGrayLevels(image);

        double bin[] = new double[binSize];
        double binNormalized[] = new double[binSize];
        fillBins(grayLevels,bin,numberPixels,binNormalized);

       // double[] probGrayLevels = new double[256];
      //  double[] cummulativeDistributionFunction = new double[256];
       // createProbGrayLevelsAndCummulativeDistributionFunction(numberPixels, probGrayLevels, cummulativeDistributionFunction, grayLevels);

        g2.setColor(Color.BLUE);
        double binWidth = (histogramm.getWidth()-6)/(binSize*1.);
        double cant = 1/maximumOfArray(binNormalized);
        for (int i = 0; i < binSize; i++) {

            int height = (int) (binNormalized[i]*cant*histogramm.getHeight()-6);
            g2.fillRect((int)(i*binWidth+3),(int) ((histogramm.getHeight()-3)-height), (int) binWidth,height );
        }



        BufferedImage both = Utils.concatImagesHorizontal(image,imageCorrected);
        BufferedImage all = Utils.concatImagesVertical(both,histogramm);

        images[paintMode.ordinal()] = all;
        taskNaration[paintMode.ordinal()] = "Histogramm:\nBins: " + binSize + "\nCant of Histograph: " + cant;


    }

    private double maximumOfArray(double[] someArray) {
        double maximum = 0;
        for (int i = 0; i < someArray.length; i++) {
            if (someArray[i]> maximum) maximum = someArray[i];
        }
        return maximum;
    }

    private void fillBins(double[] grayLevels, double[] bin, int numberPixels, double[] binNormalized) {
        int[] binSeperator = createBinSeperator(bin.length,256);
        int s = 0;
        int seperator = binSeperator[s];
        for (int i = 0; i < grayLevels.length; i++) {
            if (i>seperator) {
                seperator = binSeperator[++s];
            }
            bin[s] += grayLevels[i];
        }
        double sum = 0;
        for (int i = 0; i < binNormalized.length; i++) {
            binNormalized[i] = bin[i] /numberPixels;
            sum +=binNormalized[i];
        }
        sum+=0;
    }

    private int[] createBinSeperator(int binSize, int maxColorValue) {
        int[] seperators = new int[binSize];
        double binWidth = (maxColorValue*1.)/binSize;
        for (int i = 1; i <= binSize; i++) {
            seperators[i-1] = (int) (binWidth * i);
        }
        return seperators;
    }

    private void createProbGrayLevelsAndCummulativeDistributionFunction(int numberPixels, double[] probGrayLevels, double[] cummulativeDistributionFunction, double[] grayLevels) {

        double sum = 0;
        for (int i = 0; i < 256; i++) {
            probGrayLevels[i]=grayLevels[i]/numberPixels;
            sum += probGrayLevels[i];
            cummulativeDistributionFunction[i] = sum;
        }
    }

    private double[] getGrayLevels(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        double[] grayLevels = new double[256];
        for (int i = 0; i < 256; i++) {
            grayLevels[i]=0;
            //probGrayLevels[i]=0;
        }
        Color c;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                c = new Color(image.getRGB(x,y));
                int value = c.getBlue();
                grayLevels[value]++;

            }
        }
        return grayLevels;
    }

    private int[] createBinSizes(int i) {
        int[] th = new int[i];
        for (int j = 1; j < 6; j++) {
            th[j-1] = (256/16)*j;
            taskNaration[PaintMode.task1_51.ordinal()-1+j] = "Binsize: " +  th[j-1] + "\n";
        }

        return th;
    }


    public int[] getContrastSketichingInputValues(BufferedImage b1_3) {
        int[] contrastSketchingInputValues = null;
        while (contrastSketchingInputValues == null) {
            contrastSketchingInputValues = new int[4];
            String inputVariable[] = new String[4];
            inputVariable[0] = "a";
            inputVariable[1] = "b";
            inputVariable[2] = "ya";
            inputVariable[3] = "yb";
            double[] exampleValues = new double[4];
            exampleValues[0] = 50;
            exampleValues[1] = 150;
            exampleValues[2] = 30;
            exampleValues[3] = 200;
            double[] contrastSketchingInputValuesDef = showInputDialogWithSomeOptionalBufferedImmage(b1_3, inputVariable, exampleValues, "Contrast Stretching\n0<= a <  b<= 255;0<= ya < yb <= 255");
            for (int i = 0; i < contrastSketchingInputValuesDef.length; i++) {
                contrastSketchingInputValues[i] = (int) contrastSketchingInputValuesDef[i];
            }
            if (contrastSketchingInputValues[0] < 0 || contrastSketchingInputValues[0] > 255)
                contrastSketchingInputValues = null;
            if (contrastSketchingInputValues[1] < 0 || contrastSketchingInputValues[1] > 255)
                contrastSketchingInputValues = null;
        }
        return contrastSketchingInputValues;
    }

    private double[] getClippingInputValues(BufferedImage b1_3) {
        double[] clipping = null;
        while (clipping == null) {
            clipping = new double[3];
            String inputVariable[] = new String[3];
            inputVariable[0] = "a";
            inputVariable[1] = "b";
            inputVariable[2] = "beta";
            double[] exampleValues = new double[3];
            exampleValues[0] = 50;
            exampleValues[1] = 150;
            exampleValues[2] = 2;
            double[] clippingInputValuesDef = showInputDialogWithSomeOptionalBufferedImmage(b1_3, inputVariable, exampleValues, "Clipping\n0<=a < b <=255 ; beta is the gradient between a and b");
            for (int i = 0; i < clippingInputValuesDef.length; i++) {
                clipping[i] = clippingInputValuesDef[i];
            }
            if (clipping[0] < 0 || clipping[0] > 255)
                clipping = null;
            if (clipping[1] < 0 || clipping[1] > 255)
                clipping = null;
            if (clipping[2] < 0) clipping = null;
        }
        return clipping;
    }
    private double[] getRangeCompressionInputValues(BufferedImage b1_3) {
        double[] range = null;
        while (range == null) {
            range = new double[1];
            String inputVariable[] = new String[1];
            inputVariable[0] = "c";

            double[] exampleValues = new double[1];
            exampleValues[0] = 100;

            double[] rangeInputValuesDef = showInputDialogWithSomeOptionalBufferedImmage(b1_3, inputVariable, exampleValues, "0<=c");
            for (int i = 0; i < rangeInputValuesDef.length; i++) {
                range[i] = rangeInputValuesDef[i];
            }
            if (range[0] < 0 || range[0] > 255)
                range = null;
         //   if (range[1] < 0 || range[1] > 255)
          //      range = null;
        }
        return range;
    }

    private void task1_4(BufferedImage b1_3) {
        //assumes maximum value is 255
        int[] contrastSketchingInputValues = getContrastSketichingInputValues(b1_3);
        contrastSketching(b1_3,contrastSketchingInputValues);
        double[] clippingInputValues = getClippingInputValues(b1_3);
        clipping(b1_3,clippingInputValues);
        double[] rangeCompressionInputValues = getRangeCompressionInputValues(b1_3);
        rangeCompression(b1_3,rangeCompressionInputValues);

    }

    private void rangeCompression(BufferedImage b1_3, double[] rangeCompressionInputValues) {
        BufferedImage rc = Utils.copyImage(b1_3);

        //BufferedImage bi1 =  images[PaintMode.task1_1.ordinal()];
        int w = b1_3.getWidth();
        int h = b1_3.getHeight();
        double cVal = rangeCompressionInputValues[0];


        Color c;
        Color n;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                c = new Color(b1_3.getRGB(x,y));
                int newVal = 0;
                int xval = (c.getBlue());
                double variable = 1 + xval;
                newVal = (int) (cVal* Math.log10(variable));
                n = new Color(newVal,newVal,newVal);
                rc.setRGB(x,y,n.getRGB());
            }

        }


        images[PaintMode.rangeCompression.ordinal()] = Utils.concatImagesHorizontal(b1_3,rc);


        taskNaration[PaintMode.rangeCompression.ordinal()] = "Range Compression:\n";
        for (int i = 0; i < rangeCompressionInputValues.length; i++) {
            taskNaration[PaintMode.rangeCompression.ordinal()] += "Variable " + i +": " + rangeCompressionInputValues[i] + "\n";
        }
    }

    private void clipping(BufferedImage b1_3, double[] clippingInputValues) {
        BufferedImage clipped = Utils.copyImage(b1_3);

        //BufferedImage bi1 =  images[PaintMode.task1_1.ordinal()];
        int w = b1_3.getWidth();
        int h = b1_3.getHeight();


        double a= clippingInputValues[0];
        double b = clippingInputValues[1];
        double beta = clippingInputValues[2];





        Color c;
        Color n;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                c = new Color(b1_3.getRGB(x,y));
                int newVal = 0;
                int xval = (c.getBlue());
                if (xval>=0&&x<a){
                    newVal = 0;
                }
                if (xval>=a&&xval<b){
                    newVal = (int) (beta*(xval-a));
                }
                if (b <= xval){
                    newVal = (int) (beta*(b-a));
                }

                //try{
                n = new Color(newVal,newVal,newVal);
                clipped.setRGB(x,y,n.getRGB());
            }

        }


        images[PaintMode.clipping.ordinal()] = Utils.concatImagesHorizontal(b1_3,clipped);

        taskNaration[PaintMode.clipping.ordinal()] = "Clipping:\n";
        for (int i = 0; i < clippingInputValues.length; i++) {
            taskNaration[PaintMode.clipping.ordinal()] += "Variable " + i +": " + clippingInputValues[i] + "\n";
        }
    }

    private void contrastSketching(BufferedImage b1_3, int[] contrastSketchingInputValues) {
        BufferedImage sk = Utils.copyImage(b1_3);

        //BufferedImage bi1 =  images[PaintMode.task1_1.ordinal()];
        int w = b1_3.getWidth();
        int h = b1_3.getHeight();

        double a= contrastSketchingInputValues[0];
        double b = contrastSketchingInputValues[1];
        double ya = (contrastSketchingInputValues[2]);
        double yb = contrastSketchingInputValues[3];
        double alpha = ya/a;
        double beta = (yb-ya)/(b-a);
        double gamma = (255-yb)/(255-b);
            BufferedImage bi = Utils.copyImage(b1_3);

            Color c;
            Color n;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    c = new Color(b1_3.getRGB(x,y));
                    int newVal = 0;
                   int xval = (c.getBlue());
                   if (xval>=0&&x<a){
                       newVal = (int) (alpha*xval);
                   }
                   if (xval>=a&&xval<b){
                       newVal = (int) (beta*(xval-a)+ya);
                   }
                   if (b <= xval){
                       newVal = (int) (gamma*(xval-b)+yb);
                   }

                        //try{
                            n = new Color(newVal,newVal,newVal);
                            sk.setRGB(x,y,n.getRGB());
                        //}catch (Exception e){
                        //    e.printStackTrace();
                        //}


                    // System.out.println(level);
                   // if (level< thress){
                   //     n = new Color(0,0,0);
                   //     bi.setRGB(x,y,n.getRGB());
                   // }
                   // if (level>= thress){
                   //     n = new Color(255,255,255);
                   //     bi.setRGB(x,y,n.getRGB());
                    }

                }


            images[PaintMode.contrastSketching.ordinal()] = Utils.concatImagesHorizontal(b1_3,sk);

            taskNaration[PaintMode.contrastSketching.ordinal()] = "Contrast Sketching:\n";
                for (int i = 0; i < contrastSketchingInputValues.length; i++) {
                    taskNaration[PaintMode.contrastSketching.ordinal()] += "Variable " + i +": " + contrastSketchingInputValues[i] + "\n";
                }

    }




    /**
     *
     * @param someImage any Buffered Image will do
     * @param inputVariable the names which will be displayed
     * @param exampleValues the example values which will be displayed
     * @param comment the comments for the user
     * @return
     */
    private double[] showInputDialogWithSomeOptionalBufferedImmage(BufferedImage someImage, String[] inputVariable, double[] exampleValues, String comment) {
        double[] resultvalue = new double[inputVariable.length];
            JTextField[] field = new JTextField[inputVariable.length];

            JPanel myPanel = new JPanel();
            myPanel.setLayout(new BoxLayout(myPanel,BoxLayout.Y_AXIS));
          if (someImage!=null) {

              ImageIcon image = new ImageIcon(someImage);
              JLabel lbl = new JLabel(image);
              myPanel.add(lbl);
          }
        for (int i = 0; i < field.length; i++) {
            JPanel line = new JPanel();
            line.setLayout(new BoxLayout(line,BoxLayout.X_AXIS));
            field[i] = new JTextField(5);
            line.add(new JLabel(inputVariable[i]));
            line.add(field[i]);
            field[i].setText(String.valueOf(exampleValues[i]));
            myPanel.add(line);
            myPanel.add(Box.createHorizontalStrut(15)); // a spacer

        }

        myPanel.add(new JLabel(Utils.textConvertForJLabel(comment)));

            int result = JOptionPane.showConfirmDialog(null, myPanel,
                    "Please Enter X and Y Values", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                for (int i = 0; i < inputVariable.length; i++) {
                    resultvalue[i] =  Double.parseDouble(field[i].getText());
                }
            }else resultvalue = null;


        return resultvalue;
    }

    private void initImages(BufferedImage bi) {
        for (int i = 0; i < PaintMode.values().length; i++) {
            images[i] = bi;
        }
    }
    private void initNaration() {
        for (int i = 0; i < PaintMode.values().length; i++) {
            taskNaration[i] = "";
        }

    }

    private void task1_3() {
        BufferedImage b1_3 = images[PaintMode.task1_2.ordinal()];
        int[] thresholds = createStandartThreshholds(5);
        //BufferedImage bi1 =  images[PaintMode.task1_1.ordinal()];
        int w = b1_3.getWidth();
        int h = b1_3.getHeight();
        for (int i = 0; i < thresholds.length; i++) {
            BufferedImage bi = Utils.copyImage(b1_3);
            int thress = thresholds[i];
            Color c;
            Color n;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    c = new Color(b1_3.getRGB(x,y));
                    int level = (c.getBlue());
                   // System.out.println(level);
                    if (level< thress){
                       n = new Color(0,0,0);
                       bi.setRGB(x,y,n.getRGB());
                    }
                    if (level>= thress){
                        n = new Color(255,255,255);
                        bi.setRGB(x,y,n.getRGB());
                    }

                }

            }
            images[PaintMode.task1_31.ordinal()+i] = Utils.concatImagesHorizontal(b1_3,bi);
            taskNaration[PaintMode.task1_31.ordinal()+i] = "Some name: " + (1+i)+"\n"  + taskNaration[PaintMode.task1_31.ordinal()+i] ;
        }








    }

    private int[] createStandartThreshholds(int i) {
        int[] th = new int[i];
        for (int j = 1; j < i+1; j++) {
            th[j-1] = (256/i)*j -1;
            taskNaration[PaintMode.task1_31.ordinal()-1+j] = "Threshhold: " +  th[j-1] + "\n";
        }

        return th;
    }

    private void task1_1(String s, PaintMode pm) {
        BufferedImage bi1;
        if (s == null) bi1 = Utils.loadBufferedImageWithDialog();
        else bi1 = Utils.loadBufferedImage(s);
        taskNaration[pm.ordinal()] = "LoadedImage: " + s;
        images[pm.ordinal()] = bi1;
        pm.setImage(bi1);
        pm.setNaration("LoadedImage: " + s);
    }
    private void task1_2() {
        BufferedImage bi1 =  images[PaintMode.task1_1.ordinal()];
        int w = bi1.getWidth();
        int h = bi1.getHeight();

        BufferedImage bi1_2 = transFormToGrayScale(bi1);
        images[PaintMode.task1_2.ordinal()] = bi1_2;
        taskNaration[PaintMode.task1_2.ordinal()] = "Gray Scaled\nWidth: " + w + "\nHeight: " + h;
    }

    private BufferedImage transFormToGrayScale(BufferedImage bi1) {
        BufferedImage bi1_2 = Utils.copyImage(bi1);
        int w = bi1.getWidth();
        int h = bi1.getHeight();
        
        Graphics g = bi1.createGraphics();
        g.drawImage(bi1, 0, 0, null);
        g.dispose();
        Color c;
        Color n;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                c = new Color(bi1.getRGB(x,y));
                int average = (c.getBlue() + c.getRed() + c.getGreen()) / 3;
                n = new Color(average,average,average);
                bi1_2.setRGB(x,y,n.getRGB());
            }

        }
        return bi1_2;
    }

    private void setWindowSize() {
        BufferedImage imgToDraw = null;
        imgToDraw = images[paintMode.ordinal()];
        int widht = imgToDraw.getWidth();
        int height = imgToDraw.getHeight();
        this.getPaintPanel().setSize(widht,height);
        this.getPaintPanel().setPreferredSize(new Dimension(widht,height));
        this.getPaintWindow().pack();
    }

    private MouseListener createMouseListener(JFrame paintWindow) {
        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean b = false;
                if (e.getButton() == 1) b=true;
                flipPaintMode(b);

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
    }

    private void flipTextComment() {
       int i=  (paintMode.ordinal());
        label.setText(Utils.textConvertForJLabel(taskNaration[i]));
       paintWindow.pack();
    }

    private void flipPaintMode(boolean forward) {
        int additor = -1;
        if (forward) additor = 1;
        int newThingi = (paintMode.ordinal() + additor) % PaintMode.values().length;
        if (newThingi<0) newThingi = PaintMode.values().length-1;
        paintMode = PaintMode.values()[newThingi];
        paintWindow.repaint();
    }

    private KeyListener createKeyListener(JFrame paintWindow) {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                flipPaintMode(true);

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        };
    }

    public static void main(String[] args) {
        //"image1.jpg";"image2.jpg";"image3.png"homer.jpg
        //ImageToSoundProcessor ip = new ImageToSoundProcessor("grayscales.jpg","image2.jpg","image3.png");
        ImageToSoundProcessor ip = new ImageToSoundProcessor("homer.jpg","image2.jpg","image3.png");
        ip.setVisible(true);
    }


    @Override
    public JFrame getPaintWindow() {
        return paintWindow;
    }

    @Override
    public void setPaintWindow(JFrame paintWindow) {
        this.paintWindow = paintWindow;
    }

    @Override
    public void paint(Graphics g) {

       this.setWindowSize();
       // this.getPaintWindow().setSize(widht,height);
        BufferedImage imgToDraw = null;
        imgToDraw = images[paintMode.ordinal()];
        flipTextComment();

       // g.setColor(Color.blue);
        //g.fillRect(0,0,300,300);
        g.drawImage(imgToDraw,0,0,null);
    }

    @Override
    public void setPaintPanel(PaintPanel<Visualizable> pp) {
        this.panel = pp;
    }

    @Override
    public JPanel getPaintPanel() {
        return panel;
    }
}
