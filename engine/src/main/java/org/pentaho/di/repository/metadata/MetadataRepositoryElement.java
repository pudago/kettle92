/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.repository.metadata;

import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;

public class MetadataRepositoryElement implements RepositoryElementInterface {

  @Override
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    
    return null;
  }

  @Override
  public void setRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectory ) {
    

  }

  @Override
  public String getName() {
    
    return null;
  }

  @Override
  public void setName( String name ) {
    

  }

  @Override
  public String getDescription() {
    
    return null;
  }

  @Override
  public void setDescription( String description ) {
    

  }

  @Override
  public ObjectId getObjectId() {
    
    return null;
  }

  @Override
  public void setObjectId( ObjectId id ) {
    

  }

  @Override
  public RepositoryObjectType getRepositoryElementType() {
    
    return null;
  }

  @Override
  public ObjectRevision getObjectRevision() {
    
    return null;
  }

  @Override
  public void setObjectRevision( ObjectRevision objectRevision ) {
    

  }

}
