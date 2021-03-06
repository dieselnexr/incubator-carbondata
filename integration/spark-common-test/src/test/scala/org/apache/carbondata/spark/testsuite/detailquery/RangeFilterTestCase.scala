/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.carbondata.spark.testsuite.detailquery

import org.apache.spark.sql.Row
import org.apache.spark.sql.common.util.QueryTest
import org.scalatest.BeforeAndAfterAll

import org.apache.carbondata.core.constants.CarbonCommonConstants
import org.apache.carbondata.core.util.CarbonProperties

/**
 * Test Class for Range Filters.
 */
class RangeFilterTestCase extends QueryTest with BeforeAndAfterAll {

  override def beforeAll {
    //For the Hive table creation and data loading
    sql("drop table if exists filtertestTable")
    sql("drop table if exists NO_DICTIONARY_HIVE_1")
    sql("drop table if exists NO_DICTIONARY_CARBON_1")
    sql("drop table if exists NO_DICTIONARY_CARBON_2")
    sql("drop table if exists NO_DICTIONARY_HIVE_6")
    sql("drop table if exists NO_DICTIONARY_HIVE_7")
    sql("drop table if exists NO_DICTIONARY_CARBON_6")
    sql("drop table if exists DICTIONARY_CARBON_6")
    sql("drop table if exists NO_DICTIONARY_CARBON_7")

    sql("CREATE TABLE NO_DICTIONARY_HIVE_1 (CUST_ID int,CUST_NAME String,ACTIVE_EMUI_VERSION " +
        "string, DOB timestamp, DOJ timestamp, BIGINT_COLUMN1 bigint,BIGINT_COLUMN2 bigint," +
        "DECIMAL_COLUMN1 decimal(30,10), DECIMAL_COLUMN2 decimal(36,10),Double_COLUMN1 double, " +
        "Double_COLUMN2 double,INTEGER_COLUMN1 int) row format delimited fields terminated by ',' " +
        "TBLPROPERTIES (\"skip.header.line.count\"=\"1\") ")

    sql(
      "CREATE TABLE NO_DICTIONARY_CARBON_1 (CUST_ID int,CUST_NAME String,ACTIVE_EMUI_VERSION " +
      "string, DOB " +
      "timestamp, DOJ timestamp, BIGINT_COLUMN1 bigint,BIGINT_COLUMN2 bigint,DECIMAL_COLUMN1 " +
      "decimal(30,10), DECIMAL_COLUMN2 decimal(36,10),Double_COLUMN1 double, Double_COLUMN2 " +
      "double,INTEGER_COLUMN1 int) STORED BY 'org.apache.carbondata.format' TBLPROPERTIES " +
      "('DICTIONARY_EXCLUDE'='CUST_NAME')")

    sql(
      "create table NO_DICTIONARY_HIVE_6(empno string,empname string,designation string,doj " +
      "Timestamp,workgroupcategory int, " +
      "workgroupcategoryname string,deptno int, deptname string, projectcode int, " +
      "projectjoindate Timestamp,projectenddate Timestamp,attendance int, "
      + "utilization int,salary int) row format delimited fields terminated by ',' " +
      "tblproperties(\"skip.header.line.count\"=\"1\") " +
      ""
    )
    sql(
      s"load data local inpath '$resourcesPath/datawithoutheader.csv' into table " +
      "NO_DICTIONARY_HIVE_6"
    );

    sql(
      "create table NO_DICTIONARY_HIVE_7(empno string,empname string,designation string,doj " +
      "Timestamp,workgroupcategory int, " +
      "workgroupcategoryname string,deptno int, deptname string, projectcode int, " +
      "projectjoindate Timestamp,projectenddate Timestamp,attendance int, "
      + "utilization int,salary int) row format delimited fields terminated by ',' " +
      "tblproperties(\"skip.header.line.count\"=\"1\") " +
      ""
    )
    sql(
      s"load data local inpath '$resourcesPath/datawithoutheader.csv' into table " +
      "NO_DICTIONARY_HIVE_7"
    );
    //For Carbon cube creation.
    sql("CREATE TABLE NO_DICTIONARY_CARBON_6 (empno string, " +
        "doj Timestamp, workgroupcategory Int, empname String,workgroupcategoryname String, " +
        "deptno Int, deptname String, projectcode Int, projectjoindate Timestamp, " +
        "projectenddate Timestamp, designation String,attendance Int,utilization " +
        "Int,salary Int) STORED BY 'org.apache.carbondata.format' " +
        "TBLPROPERTIES('DICTIONARY_EXCLUDE'='empno, empname,designation')"
    )
    sql(
      s"LOAD DATA LOCAL INPATH '$resourcesPath/data.csv' INTO TABLE NO_DICTIONARY_CARBON_6 " +
      "OPTIONS('DELIMITER'= ',', 'QUOTECHAR'= '\"')"
    )

    //For Carbon cube creation.
    sql("CREATE TABLE DICTIONARY_CARBON_6 (empno string, " +
        "doj Timestamp, workgroupcategory Int, empname String,workgroupcategoryname String, " +
        "deptno Int, deptname String, projectcode Int, projectjoindate Timestamp, " +
        "projectenddate Timestamp, designation String,attendance Int,utilization " +
        "Int,salary Int) STORED BY 'org.apache.carbondata.format' " +
        "TBLPROPERTIES('DICTIONARY_EXCLUDE'='empname,designation')"
    )
    sql(
      s"LOAD DATA LOCAL INPATH '$resourcesPath/data.csv' INTO TABLE DICTIONARY_CARBON_6 " +
      "OPTIONS('DELIMITER'= ',', 'QUOTECHAR'= '\"')"
    )


    sql("CREATE TABLE NO_DICTIONARY_CARBON_7 (empno string, " +
        "doj Timestamp, workgroupcategory Int, empname String,workgroupcategoryname String, " +
        "deptno Int, deptname String, projectcode Int, projectjoindate Timestamp, " +
        "projectenddate Timestamp, designation String,attendance Int,utilization " +
        "Int,salary Int) STORED BY 'org.apache.carbondata.format' " +
        "TBLPROPERTIES('DICTIONARY_EXCLUDE'='empno,empname,designation')"
    )
    sql(
      s"LOAD DATA LOCAL INPATH '$resourcesPath/data.csv' INTO TABLE NO_DICTIONARY_CARBON_7 " +
      "OPTIONS('DELIMITER'= ',', 'QUOTECHAR'= '\"')"
    )
    sql("CREATE TABLE filtertestTable (ID string,date Timestamp, country String, " +
        "name String, phonetype String, serialname String, salary Int) " +
        "STORED BY 'org.apache.carbondata.format' " +  "TBLPROPERTIES('DICTIONARY_EXCLUDE'='ID')"
    )
    CarbonProperties.getInstance()
      .addProperty(CarbonCommonConstants.CARBON_TIMESTAMP_FORMAT, "yyyy-MM-dd HH:mm:ss")
    sql(
      s"LOAD DATA LOCAL INPATH '$resourcesPath/data2.csv' INTO TABLE filtertestTable OPTIONS"+
      s"('DELIMITER'= ',', " +
      s"'FILEHEADER'= '')"
    );


  }

  test("Range filter No Dictionary 1") {
    sql("select * from NO_DICTIONARY_CARBON_6").show()
    sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' and empno < '15'").show()
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' and empno < '15'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '15'")
    )
  }

  test("Range filter No Dictionary outside Boundary before block") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '00' and empno < '09'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '00' and empno < '09'")
    )
  }


  test("Range filter No Dictionary outside Boundary after block") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '22' and empno < '30'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '22' and empno < '30'")
    )
  }

  test("Range filter No Dictionary Inside Boundary") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '00' and empno < '30'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '00' and empno < '30'")
    )
  }

  test("Range filter No Dictionary outside Boundary 1") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '20' and empno <= '30'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno >= '20' and empno <= '30'")
    )
  }


  test("Range filter No Dictionary outside Boundary 2") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '20' and empno <= '30'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '20' and empno <= '30'")
    )
  }


  test("Range filter No Dictionary outside Boundary 3") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno <= '11' and empno > '00'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno <= '11' and empno > '00'")
    )
  }


  test("Range filter No Dictionary outside Boundary 4") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno < '11' and empno > '00'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno < '11' and empno > '00'")
    )
  }

  test("Range filter No Dictionary outside Boundary 5") {
    sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno < '11' and empno > '20'").show()
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno < '11' and empno > '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno < '11' and empno > '20'")
    )
  }

  test("Range filter No Dictionary outside Boundary 6") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno <= '11' and empno > '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno <= '11' and empno > '20'")
    )
  }

  test("Range filter No Dictionary outside Boundary 7") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno < '11' and empno >= '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno < '11' and empno >= '20'")
    )
  }


  test("Range filter No Dictionary outside Boundary 8") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno <= '11' and empno >= '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno <= '11' and empno >= '20'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 9") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno <= '11' and empno >= '11'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno <= '11' and empno >= '11'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 10") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno <= '20' and empno >= '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno <= '20' and empno >= '20'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 11") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno <= '15' and empno >= '15'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno <= '15' and empno >= '15'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 12") {
    sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' and empno > '12' and empno <= '20' and empno <= '15'").show()
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' and empno > '12' and empno <= '20' and empno <= '15'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno >= '11' and empno > '12' and empno <= '20'  and empno <= '15'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 13") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' and empno > '12' and empno <= '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno >= '11' and empno > '12' and empno <= '20'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 14") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' and empno > '12' and empno <= '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno >= '11' and empno > '12' and empno <= '20'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 15") {
    sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' or empno > '12' and empno <= '20'").show()
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' or empno > '12' and empno <= '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno >= '11' or empno > '12' and empno <= '20'")
    )
  }

  test("Range filter No Dictionary Inside Boundary 16") {
    sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' and empno > '12' or empno <= '20'").show()
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno >= '11' and empno > '12' or empno <= '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno >= '11' and empno > '12' or empno <= '20'")
    )
  }

  test("Range filter No Dictionary duplicate filters 1") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' and empno > '11' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno > '11' and empno < '20'")
    )
  }

  test("Range filter No Dictionary duplicate filters2") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' or empno > '11' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' or empno > '11' and empno < '20'")
    )
  }

  test("Range filter No Dictionary duplicate filters3") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' or empno > '11' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' or empno > '11' and empno < '20'")
    )
  }

  test("Range filter No Dictionary multiple filters") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' and workgroupcategory = '1' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and workgroupcategory = '1' and empno < '20'")
    )
  }

  test("Range filter No Dictionary multiple filters1") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' and empno < '20' and workgroupcategory = '1'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '20' and workgroupcategory = '1'")
    )
  }

  test("Range filter No Dictionary multiple filters2") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' and empno < '13' and workgroupcategory = '1' and empno > '14' and empno < '17'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '13' and workgroupcategory = '1' and empno > '14' and empno < '17'")
    )
  }


  test("Range filter No Dictionary multiple filters3") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_CARBON_6 where empno > '11' and empno < '13' and workgroupcategory = '1' or empno > '14' or empno < '17'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '13' and workgroupcategory = '1' or empno > '14' or empno < '17'")
    )
  }

  test("Range filter Dictionary 1") {
    sql("select * from DICTIONARY_CARBON_6").show()
    // sql("select * from NO_DICTIONARY_CARBON_6 where empno != '16'").show()
    sql("select * from DICTIONARY_CARBON_6 where empno > '11' and workgroupcategory = '1' or workgroupcategoryname = 'developer' and empno < '15'").show()
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' and empno < '15'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '15'")
    )
  }

  test("Range filter Dictionary outside Boundary before block") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '00' and empno < '09'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '00' and empno < '09'")
    )
  }


  test("Range filter Dictionary outside Boundary after block") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '22' and empno < '30'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '22' and empno < '30'")
    )
  }

  test("Range filter Dictionary Inside Boundary") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '00' and empno < '30'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '00' and empno < '30'")
    )
  }

  test("Range filter Dictionary outside Boundary") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno < '11' and empno > '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno < '11' and empno > '20'")
    )
  }

  test("Range filter Dictionary duplicate filters1") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' and empno > '11' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno > '11' and empno < '20'")
    )
  }

  test("Range filter Dictionary duplicate filters2") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' or empno > '11' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' or empno > '11' and empno < '20'")
    )
  }

  test("Range filter Dictionary duplicate filters3") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' or empno > '11' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' or empno > '11' and empno < '20'")
    )
  }

  test("Range filter Dictionary multiple filters") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' and workgroupcategory = '1' and empno < '20'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and workgroupcategory = '1' and empno < '20'")
    )
  }

  test("Range filter Dictionary multiple filters1") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' and empno < '20' and workgroupcategory = '1'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '20' and workgroupcategory = '1'")
    )
  }

  test("Range filter Dictionary multiple filters2") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' and empno < '13' and workgroupcategory = '1' and empno > '14' and empno < '17'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '13' and workgroupcategory = '1' and empno > '14' and empno < '17'")
    )
  }


  test("Range filter Dictionary multiple filters3") {
    checkAnswer(
      sql("select empno,empname,workgroupcategory from DICTIONARY_CARBON_6 where empno > '11' and empno < '13' and workgroupcategory = '1' or empno > '14' or empno < '17'"),
      sql("select empno,empname,workgroupcategory from NO_DICTIONARY_HIVE_6 where empno > '11' and empno < '13' and workgroupcategory = '1' or empno > '14' or empno < '17'")
    )
  }


  test("Range filter with join") {
    checkAnswer(
      sql("select s.empno, s.empname, t.empno, t.empname from DICTIONARY_CARBON_6 s, NO_DICTIONARY_CARBON_6 t where s.empno > '11' and t.empno < '16' and s.empname = t.empname"),
      sql("select s.empno, s.empname, t.empno, t.empname from NO_DICTIONARY_HIVE_6 s, NO_DICTIONARY_HIVE_7 t where s.empno > '11' and t.empno < '16' and s.empname = t.empname"))
  }

  test("Range filter with join 1") {
    checkAnswer(
      sql("select s.empno, s.empname, t.empno, t.empname from DICTIONARY_CARBON_6 s, NO_DICTIONARY_CARBON_6 t where s.empno > '09' and t.empno < '30' and s.empname = t.empname"),
      sql("select s.empno, s.empname, t.empno, t.empname from NO_DICTIONARY_HIVE_6 s, NO_DICTIONARY_HIVE_7 t where s.empno > '09' and t.empno < '30' and s.empname = t.empname"))
  }


  override def afterAll {
    sql("drop table if exists filtertestTable")
    sql("drop table if exists NO_DICTIONARY_HIVE_1")
    sql("drop table if exists NO_DICTIONARY_CARBON_1")
    sql("drop table if exists NO_DICTIONARY_CARBON_2")
    sql("drop table if exists NO_DICTIONARY_HIVE_6")
    sql("drop table if exists NO_DICTIONARY_HIVE_7")
    sql("drop table if exists NO_DICTIONARY_CARBON_6")
    sql("drop table if exists DICTIONARY_CARBON_6")
    sql("drop table if exists NO_DICTIONARY_CARBON_7")
  }
}