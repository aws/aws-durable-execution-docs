import java.nio.file.Path;
import software.amazon.lambda.durable.config.StepConfig;
import software.amazon.lambda.durable.serde.Base64StringBinaryCodec;
import software.amazon.lambda.durable.serde.ComposableBinarySerDesStage;
import software.amazon.lambda.durable.serde.JacksonSerDes;
import software.amazon.lambda.durable.serde.SerDes;
import software.amazon.lambda.durable.serde.Utf8StringBinaryCodec;
import software.amazon.lambda.durable.serde.filesystem.FileSystemSerDesStage;

var base64Stage = ComposableBinarySerDesStage.builder()
    .startWith(Utf8StringBinaryCodec.INSTANCE)
    .endWith(Base64StringBinaryCodec.INSTANCE)
    .build();

var fileSystemStage = FileSystemSerDesStage
    .builder(Path.of("/mnt/efs/durable-payloads"))
    .build();

SerDes pipeline = new JacksonSerDes()
    .then(base64Stage)
    .then(fileSystemStage);

StepConfig stepConfig = StepConfig.builder()
    .serDes(pipeline)
    .build();
