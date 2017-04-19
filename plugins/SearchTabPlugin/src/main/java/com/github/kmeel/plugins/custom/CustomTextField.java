/*
 * This file is part of Kmeel.
 * Copyright (C) 2017  Marten4n6
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.kmeel.plugins.custom;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;

public class CustomTextField extends TextField {

    /**************************************************************************
     *
     * Private fields
     *
     **************************************************************************/




    /**************************************************************************
     *
     * Constructors
     *
     **************************************************************************/

    /**
     * Instantiates a default CustomTextField.
     */
    public CustomTextField() {
        getStyleClass().add("custom-text-field"); //$NON-NLS-1$
    }



    /**************************************************************************
     *
     * Properties
     *
     **************************************************************************/

    // --- left
    private ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left"); //$NON-NLS-1$

    /**
     *
     * @return An ObjectProperty wrapping the {@link Node} that is placed
     * on the left ofthe text field.
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    /**
     *
     * @return the {@link Node} that is placed on the left of
     * the text field.
     */
    public final Node getLeft() {
        return left.get();
    }

    /**
     * Sets the {@link Node} that is placed on the left of
     * the text field.
     * @param value
     */
    public final void setLeft(Node value) {
        left.set(value);
    }


    // --- right
    private ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right"); //$NON-NLS-1$

    /**
     * Property representing the {@link Node} that is placed on the right of
     * the text field.
     * @return An ObjectProperty.
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    /**
     *
     * @return The {@link Node} that is placed on the right of
     * the text field.
     */
    public final Node getRight() {
        return right.get();
    }

    /**
     * Sets the {@link Node} that is placed on the right of
     * the text field.
     * @param value
     */
    public final void setRight(Node value) {
        right.set(value);
    }



    /**************************************************************************
     *
     * Public API
     *
     **************************************************************************/

    /**
     * {@inheritDoc}
     */
    @Override protected Skin<?> createDefaultSkin() {
        return new CustomTextFieldSkin(this) {
            @Override public ObjectProperty<Node> leftProperty() {
                return CustomTextField.this.leftProperty();
            }

            @Override public ObjectProperty<Node> rightProperty() {
                return CustomTextField.this.rightProperty();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override public String getUserAgentStylesheet() {
        return CustomTextField.class.getClassLoader().getResource("customtextfield.css").toExternalForm(); //$NON-NLS-1$
    }
}
