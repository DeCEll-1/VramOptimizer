package DeCell.VOpt;

import java.time.OffsetDateTime;

public class FileMetadata {
    public String ModID;
    public String ModFolderName;
    public String RelativeImagePath;
    public ImageFileType ImageType;
    public OffsetDateTime ImageCreationDate;
    public OffsetDateTime ImageEditDateDate;
    public String DDSFilePath;
    public OffsetDateTime DDSCreationDate;
    public OffsetDateTime DDSEditDate;
    public String CompressionFormat;
    public int Width;
    public int Height;

    public enum ImageFileType {
        None,
        Jpg,
        Png,
        Webp
    }
}
