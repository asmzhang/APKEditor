/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
  */
package com.reandroid.apkeditor.protect;

import com.reandroid.arsc.chunk.ChunkType;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.chunk.xml.ResXmlElementApi;
import com.reandroid.arsc.chunk.xml.UnknownResXmlNode;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.IntegerItem;
import com.reandroid.arsc.item.ResXmlString;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.utils.NumbersUtil;
import com.reandroid.utils.collection.CollectionUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * AndroidManifest.xml混淆器类
 * 负责对AndroidManifest.xml文件进行混淆处理
 */
public class ManifestConfuser extends Confuser {

    /**
     * 构造函数
     * @param protector 保护器对象
     */
    public ManifestConfuser(Protector protector) {
        super(protector, "ManifestConfuser: ");
    }

    /**
     * 执行混淆操作
     */
    @Override
    public void confuse() {
        // 如果选项指定跳过清单文件，则返回
        if (getOptions().skipManifest) {
            logMessage("Skip");
            return;
        }
        
        // 获取AndroidManifest.xml块
        AndroidManifestBlock manifestBlock = getApkModule().getAndroidManifest();
        
        // 1. 添加无效块
        placeBadChunk(manifestBlock);
        
        // 2. 混淆属性
        confuseAttributes(manifestBlock);
        
        // 3. 混淆偏移量
        confuseOffset(manifestBlock);
        
        // 刷新清单块
        manifestBlock.refresh();
    }

    /**
     * 混淆属性
     * @param manifestBlock AndroidManifest.xml块
     */
    private void confuseAttributes(AndroidManifestBlock manifestBlock) {
        int defaultAttributeSize = 20;
        List<ResXmlElement> elementList = CollectionUtil.toList(manifestBlock.recursiveElements());
        Random random = new Random();
        
        // 为每个元素添加随机属性
        for (ResXmlElement element : elementList) {
            // 设置随机属性大小
            int size = defaultAttributeSize + random.nextInt(6) + 1;
            element.setAttributesUnitSize(size, false);
            
            // 添加虚假属性
            ResXmlAttribute attribute = element.newAttribute();
            attribute.setName(" >\n  </" + element.getName() + ">\n  android:name", 0);
            attribute.setValueAsBoolean(false);
        }
        
        // 设置清单元素的属性大小
        manifestBlock.getManifestElement().setAttributesUnitSize(
                defaultAttributeSize, false);
    }

    /**
     * 混淆偏移量
     * @param manifestBlock AndroidManifest.xml块
     */
    private void confuseOffset(AndroidManifestBlock manifestBlock) {
        ResXmlElement manifest = manifestBlock.getManifestElement();
        Iterator<ResXmlElement> iterator = manifest.getElements();
        while (iterator.hasNext()) {
            confuseOffset(iterator.next());
        }
        confuseOffset(manifest);
    }

    /**
     * 混淆单个元素的偏移量
     * @param element XML元素
     */
    private void confuseOffset(ResXmlElement element) {
        ResXmlElementApi elementApi = new ResXmlElementApi(element);

        // 计算属性数组大小
        int size = elementApi.getAttributeArray().countBytes() + 1;

        // 创建随机字节数组
        ByteArray byteArray = new ByteArray();
        byteArray.setSize(size);
        
        // 每隔4个字节设置一个随机值
        for (int i = 0; i < size; i = i + 4) {
            byteArray.putInteger(i, -1);
        }
        
        // 将字节数组设置为占位符
        elementApi.getStartElement()
                .getFirstPlaceHolder()
                .setItem(byteArray);

        // 刷新元素
        element.refresh();
    }
    private void placeBadChunk(AndroidManifestBlock manifestBlock) {
        placeBadChunk(manifestBlock, ChunkType.XML_END_NAMESPACE);
        placeBadChunk(manifestBlock, ChunkType.PACKAGE);
    }
    private void placeBadChunk(AndroidManifestBlock manifestBlock, ChunkType chunkType) {
        UnknownResXmlNode unknown = manifestBlock.newUnknown();
        try {
            // 尝试读取随机字符串池
            unknown.readBytes(new BlockReader(
                    randomStringPool(chunkType)));
        } catch (IOException ignored) {
        }
        // 将未知块移动到开头
        manifestBlock.moveTo(unknown, 0);
    }

    /**
     * 生成随机字符串池
     * @param chunkType 块类型
     * @return 字节数组
     */
    private byte[] randomStringPool(ChunkType chunkType) {
        ResXmlDocument document = new ResXmlDocument();
        ResXmlStringPool stringPool = document.getStringPool();

        Random random = new Random();

        // 设置UTF8编码
        int size = NumbersUtil.min(20, 5 + random.nextInt(21));
        stringPool.setUtf8(size % 2 == 0);

        // 添加随机字符串
        for (int i = 0; i < size; i++) {
            String s = randomString();
            ResXmlString xml = stringPool.getOrCreate(s);
            xml.addReference(new IntegerItem());
        }

        // 刷新字符串池
        stringPool.refresh();
        stringPool.refresh();
        stringPool.getHeaderBlock().setType(chunkType);

        return stringPool.getBytes();
    }

    /**
     * 生成随机字符串
     * @return 随机字符串
     */
    private String randomString() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        int size = NumbersUtil.min(100, 15 + random.nextInt(90));
        
        // 生成随机字符
        for (int i = 0; i < size; i++) {
            char c = (char) (10 + random.nextInt(240));
            builder.append(c);
        }
        return builder.toString();
    }
}
