package top.itning.yunshu.yunshunas.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.itning.yunshu.yunshunas.entity.FileEntity;
import top.itning.yunshu.yunshunas.entity.Link;
import top.itning.yunshu.yunshunas.repository.IVideoRepository;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author itning
 * @date 2019/7/14 18:48
 */
@Controller
public class FileController {
    private static final String[] VIDEO_SUFFIX = new String[]{"mp4", "avi", "3gp", "wmv", "mkv", "mpeg", "rmvb"};

    private final IVideoRepository iVideoRepository;

    @Autowired
    public FileController(IVideoRepository iVideoRepository) {
        this.iVideoRepository = iVideoRepository;
    }

    @GetMapping("/")
    public String index(Model model, String location) throws UnsupportedEncodingException {
        if (location != null) {
            model.addAttribute("links", Link.build(location));
        }
        File[] files;
        if (location == null) {
            files = File.listRoots();
        } else {
            File file = new File(location);
            files = file.listFiles();
        }
        List<FileEntity> fileEntities;
        if (files != null) {
            fileEntities = new ArrayList<>(files.length);
            for (File f : files) {
                FileEntity fileEntity = new FileEntity();
                fileEntity.setName(f.getName());
                fileEntity.setSize(FileUtils.byteCountToDisplaySize(f.length()));
                fileEntity.setFile(f.isFile());
                fileEntity.setLocation(f.getPath());
                fileEntities.add(fileEntity);
            }
        } else {
            fileEntities = Collections.emptyList();
        }
        List<FileEntity> fileEntityList = fileEntities
                .parallelStream()
                .filter(fileEntity -> !fileEntity.isFile() || isVideoFile(fileEntity.getName()))
                .collect(Collectors.toList());
        model.addAttribute("files", fileEntityList);
        return "index";
    }

    @PostMapping("/del")
    @ResponseBody
    public void delFile(@RequestParam String location) throws IOException {
        String writeDir = iVideoRepository.getWriteDir(location);
        FileUtils.deleteDirectory(new File(writeDir));
        File file = new File(location);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        if (!file.delete()) {
            throw new RuntimeException("文件删除失败");
        }
    }

    private boolean isVideoFile(String name) {
        String suffix = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        for (String s : VIDEO_SUFFIX) {
            if (s.equals(suffix)) {
                return true;
            }
        }
        return false;
    }
}
