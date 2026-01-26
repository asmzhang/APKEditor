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

import com.reandroid.apk.ApkModule;
import com.reandroid.apkeditor.APKEditor;
import com.reandroid.arsc.ARSCLib;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.UnknownChunk;
import com.reandroid.arsc.item.ByteArray;
import com.reandroid.arsc.item.FixedLengthString;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.pool.TypeStringPool;

/**
 * 资源表混淆器类
 * 负责混淆APK资源表中的类型名称
 */
public class TableConfuser extends Confuser {

    /**
     * 构造函数
     * @param protector 保护器对象
     */
    public TableConfuser(Protector protector) {
        super(protector, "TableConfuser: ");
    }

    /**
     * 执行混淆操作
     */
    @Override
    public void confuse() {
        logMessage("Confusing ...");
		// 1. 添加未知块
        confuseWithUnknownChunk();
        // 2. 混淆类型名称
        confuseTypeNames();
    }
    
    /**
     * 使用未知块进行混淆
     * 在资源表中添加未知块以干扰分析
     */
    private void confuseWithUnknownChunk() {
        ApkModule apkModule = getApkModule();
        TableBlock tableBlock = apkModule.getTableBlock();
        
        // 创建未知块
        UnknownChunk unknownChunk = new UnknownChunk();
        FixedLengthString fixedLengthString = new FixedLengthString(256);
        
        // 设置仓库信息
        fixedLengthString.set(APKEditor.getRepo());
        ByteArray extra = unknownChunk.getHeaderBlock().getExtraBytes();
        byte[] bytes = fixedLengthString.getBytes();
        extra.setSize(bytes.length);
        extra.putByteArray(0, bytes);
        
        // 设置ARSCLib仓库信息
        fixedLengthString.set(ARSCLib.getRepo());
        extra = unknownChunk.getBody();
        bytes = fixedLengthString.getBytes();
        extra.setSize(bytes.length);
        extra.putByteArray(0, bytes);
        
        fixedLengthString.set(ARSCLib.getRepo());
        unknownChunk.refresh();
        
        // 将未知块设置为第一个占位符
        tableBlock.getFirstPlaceHolder().setItem(unknownChunk);
        tableBlock.refresh();
    }
    
    /**
     * 混淆类型名称
     * 将资源类型名称替换为其他类型名称
     */
    private void confuseTypeNames() {
        // 如果保留所有类型，则跳过
        if (isKeepAllTypes()) {
            logMessage("Skip type names");
            return;
        }
        
        logMessage("Type names ...");
        ApkModule apkModule = getApkModule();
        TableBlock tableBlock = apkModule.getTableBlock();
        
        // 遍历所有包块
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            TypeStringPool pool = packageBlock.getTypeStringPool();
            
            // 遍历所有类型字符串
            for(TypeString typeString : pool) {
                String type = typeString.get();
                String replace = getReplacement(type);
                
                // 如果替换名称与原名称相同，跳过
                if (type.equals(replace)) {
                    continue;
                }
                
                // 设置新的类型名称
                typeString.set(replace);
                logVerbose("'" + type + "' -> '" + typeString.get() + "'");
            }
        }
        tableBlock.refresh();
    }

    /**
     * 获取替换类型名称
     * @param type 原始类型名称
     * @return 替换后的类型名称
     */
    private String getReplacement(String type) {
        // 如果需要保留该类型，返回原类型
        if (isKeepType(type)) {
            return type;
        }

        String replacement;

        // 根据类型名称选择替换名称
        if ("attr".equals(type) ) {
            replacement = "style";
        } else if ("style".equals(type)) {
            replacement = "plurals";
        } else if ("id".equals(type)) {
            replacement = "attr";
        } else if ("mipmap".equals(type)) {
            replacement = "id";
        } else {
            replacement = type;
        }
        
        // 如果替换类型需要保留，则不替换
        if (isKeepType(replacement)) {
            replacement = type;
        }
        return replacement;
    }
}
