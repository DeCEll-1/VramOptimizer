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

    @Override
    public String toString() {
        return "ModImageInfo {" +
                "\n  ModID='" + ModID + '\'' +
                ",\n  ModFolderName='" + ModFolderName + '\'' +
                ",\n  RelativeImagePath='" + RelativeImagePath + '\'' +
                ",\n  ImageType=" + ImageType +
                ",\n  ImageCreationDate=" + ImageCreationDate +
                ",\n  ImageEditDateDate=" + ImageEditDateDate +
                ",\n  DDSFilePath='" + DDSFilePath + '\'' +
                ",\n  DDSCreationDate=" + DDSCreationDate +
                ",\n  DDSEditDate=" + DDSEditDate +
                ",\n  CompressionFormat='" + CompressionFormat + '\'' +
                ",\n  Width=" + Width +
                ",\n  Height=" + Height +
                "\n}";
    }
}
