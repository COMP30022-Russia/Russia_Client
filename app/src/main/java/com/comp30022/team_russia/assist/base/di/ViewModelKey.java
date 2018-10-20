//CHECKSTYLE.OFF: JavadocMethodCheck

package com.comp30022.team_russia.assist.base.di;

import android.arch.lifecycle.ViewModel;

import dagger.MapKey;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Key for distinguishing between different {@link ViewModel}s.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@MapKey
public @interface ViewModelKey {
    Class<? extends ViewModel> value();
}