/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2017 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.aster.plugin.garble.util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.Reader;

public class MybatisHelper {

    private static SqlSessionFactory updateCallbackSimpleSession;

    private static SqlSessionFactory updateCallbackOtherSession;

    private static SqlSessionFactory authSelectSimpleSession;

    private static SqlSessionFactory authSelectOtherSession;

    private static SqlSessionFactory authInsertSession;

    private static SqlSessionFactory authUpdateSimpleSession;


    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader(TestUtil.getXmlPath()
                    + "/mybatis-config-update-callback-simple.xml");
            updateCallbackSimpleSession = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader(TestUtil.getXmlPath()
                    + "/mybatis-config-update-callback-other.xml");
            updateCallbackOtherSession = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader(TestUtil.getXmlPath()
                    + "/mybatis-config-auth-select-simple.xml");
            authSelectSimpleSession = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader(TestUtil.getXmlPath()
                    + "/mybatis-config-auth-select-other.xml");
            authSelectOtherSession = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader(TestUtil.getXmlPath()
                    + "/mybatis-config-auth-insert.xml");
            authInsertSession = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        try {
            //创建SqlSessionFactory
            Reader reader = Resources.getResourceAsReader(TestUtil.getXmlPath()
                    + "/mybatis-config-auth-update-simple.xml");
            authUpdateSimpleSession = new SqlSessionFactoryBuilder().build(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Session
     */
    public static SqlSession getUpdateCallbackSimpleSession() {
        return updateCallbackSimpleSession.openSession();
    }

    /**
     * 获取Session
     */
    public static SqlSession getUpdateCallbackOtherSession() {
        return updateCallbackOtherSession.openSession();
    }

    /**
     * 获取Session
     */
    public static SqlSession getAuthSelectSimpleSession() {
        return authSelectSimpleSession.openSession();
    }

    /**
     * 获取Session
     */
    public static SqlSession getAuthSelectOtherSession() {
        return authSelectOtherSession.openSession();
    }

    /**
     * 获取Session
     */
    public static SqlSession getAuthInsertSession() {
        return authInsertSession.openSession();
    }

    /**
     * 获取Session
     */
    public static SqlSession getAuthUpdateSimpleSession() {
        return authUpdateSimpleSession.openSession();
    }


}
