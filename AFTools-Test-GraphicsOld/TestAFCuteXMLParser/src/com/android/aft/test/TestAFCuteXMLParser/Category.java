/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package com.android.aft.test.TestAFCuteXMLParser;

public class Category {

	public String id;
	public String name;
	public String description;
	public String categoryIdBis;
	public String element;
	public String initialRef;
	public boolean hasChild;

	public Category() {
	}

	public Category(String id) {
	    setId(id);
	}

	public String id() {
		return id;
	}

	public void setId(String id) {
	    this.id = id;
	}

	public String name() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String description() {
		return description;
	}

	public void setDescription(String description) {
	    this.description = description;
	}

	public boolean hasChild() {
		return hasChild;
	}

	public void setHasChild(boolean has_child) {
	    hasChild = has_child;
	}

    public void setCategoryIdBis(String categoryIdB) {
        categoryIdBis = categoryIdB;
    }

    public void setInitialRef(String ref) {
        initialRef = ref;
    }

    public void setElement(String elem) {
        element = elem;
    }

	@Override
    public String toString() {
		StringBuffer str = new StringBuffer();

		str.append("Category")
		   .append("(id: '").append(id).append("'")
		   .append(", idBis:'").append(categoryIdBis).append("'")
		   .append(", ref: '").append(initialRef).append("'")
		   .append(", name: '").append(name).append("'")
		   .append(", element: '").append(element).append("'")
		   .append(", description: '").append(description).append("')");

		return str.toString();
	}

}