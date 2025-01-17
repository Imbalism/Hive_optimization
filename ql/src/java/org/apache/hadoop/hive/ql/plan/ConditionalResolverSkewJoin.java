/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hive.ql.plan;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.exec.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.ql.session.SessionState.LogHelper;

/**
 * ConditionalResolverSkewJoin.
 *
 */
public class ConditionalResolverSkewJoin implements ConditionalResolver, Serializable {
  private static final long serialVersionUID = 1L;
  static final private Log LOG = LogFactory.getLog(ConditionalResolverCommonJoin.class.getName());	  
  static final private LogHelper console = new LogHelper(LOG);
  
  /**
   * ConditionalResolverSkewJoinCtx.
   *
   */
  public static class ConditionalResolverSkewJoinCtx implements Serializable {
    private static final long serialVersionUID = 1L;
    // we store big keys in one table into one dir, and same keys in other
    // tables into corresponding different dirs (one dir per table).
    // this map stores mapping from "big key dir" to its corresponding mapjoin
    // task.
    HashMap<String, Task<? extends Serializable>> dirToTaskMap;

    /**
     * For serialization use only.
     */
    public ConditionalResolverSkewJoinCtx() {
    }

    public ConditionalResolverSkewJoinCtx(
        HashMap<String, Task<? extends Serializable>> dirToTaskMap) {
      super();
      this.dirToTaskMap = dirToTaskMap;
    }

    public HashMap<String, Task<? extends Serializable>> getDirToTaskMap() {
      return dirToTaskMap;
    }

    public void setDirToTaskMap(
        HashMap<String, Task<? extends Serializable>> dirToTaskMap) {
      this.dirToTaskMap = dirToTaskMap;
    }
  }

  public ConditionalResolverSkewJoin() {
  }

  @Override
  public List<Task<? extends Serializable>> getTasks(HiveConf conf,
      Object objCtx) {
	assert false;
	console.printInfo("getTasks in skew join involved !!!!!!!!!!");	
    ConditionalResolverSkewJoinCtx ctx = (ConditionalResolverSkewJoinCtx) objCtx;
    List<Task<? extends Serializable>> resTsks = new ArrayList<Task<? extends Serializable>>();

    Map<String, Task<? extends Serializable>> dirToTaskMap = ctx
        .getDirToTaskMap();
    Iterator<Entry<String, Task<? extends Serializable>>> bigKeysPathsIter = dirToTaskMap
        .entrySet().iterator();
    try {
      while (bigKeysPathsIter.hasNext()) {
        Entry<String, Task<? extends Serializable>> entry = bigKeysPathsIter
            .next();
        String path = entry.getKey();
        Path dirPath = new Path(path);
        FileSystem inpFs = dirPath.getFileSystem(conf);
        FileStatus[] fstatus = inpFs.listStatus(dirPath);
        if (fstatus != null && fstatus.length > 0) {
          Task <? extends Serializable> task = entry.getValue();
          List<Task <? extends Serializable>> parentOps = task.getParentTasks();
          if(parentOps!=null){
            for(Task <? extends Serializable> parentOp: parentOps){
              //right now only one parent
              resTsks.add(parentOp);
            }
          }else{
            resTsks.add(task);
          }
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resTsks;
  }

}
