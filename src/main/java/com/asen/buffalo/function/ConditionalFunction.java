package com.asen.buffalo.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 一个可以替换大量if-else 和 switch的条件工具类
 *
 * @author Asen
 * @since 1.1.2
 **/
public class ConditionalFunction<K, R> {

	/**
	 * 默认方法，当找不到key时会调用次方法，类似switch中的default条件
	 */
	private ConditionMethod<R> defaultConditionMethod;

	/**
	 * 存放条件的执行函数
	 */
	private Map<K, ConditionMethod<R>> functionPool;

	/**
	 * 初始化条件池
	 *
	 * @param functionPool 存放条件的map
	 */
	public ConditionalFunction(Map<K, ConditionMethod<R>> functionPool) {
		this.functionPool = functionPool;
	}

	public ConditionalFunction() {
		this.functionPool = new HashMap<>();
	}

	/**
	 * 添加一个条件和触发它时需要执行的方法
	 *
	 * @param condition       触发条件
	 * @param conditionMethod 条件方法
	 * @return ConditionPool
	 */
	public ConditionalFunction<K, R> add(K condition, ConditionMethod<R> conditionMethod) {
		this.functionPool.put(condition, conditionMethod);
		return this;
	}

	/**
	 * 添加多个条件执行同一个方法
	 * 当多个条件的执行逻辑相同时可以使用此方法
	 *
	 * @param conditions      触发条件
	 * @param conditionMethod 条件方法
	 * @return ConditionPool
	 */
	public ConditionalFunction<K, R> add(List<K> conditions, ConditionMethod<R> conditionMethod) {
		conditions.forEach(k -> this.functionPool.put(k, conditionMethod));
		return this;
	}

	/**
	 * 添加一个默认方法，在找不到匹配条件时，会调用此函数执行
	 * 类似switch里的default条件
	 *
	 * @param defaultConditionMethod 条件方法
	 * @return ConditionPool
	 */
	public ConditionalFunction<K, R> addDefault(ConditionMethod<R> defaultConditionMethod) {
		this.defaultConditionMethod = defaultConditionMethod;
		return this;
	}

	/**
	 * 执行一个condition，如果pool中存在对应的condition，将会调用它的函数执行
	 * 如过不存在，将执行addDefault方法添加的默认function
	 *
	 * @param condition 需要验证的条件
	 * @return 条件执行结果
	 */
	public R doIf(K condition) {
		if (this.functionPool.containsKey(condition)) {
			return functionPool.get(condition).invoke();
		}
		if (Objects.nonNull(this.defaultConditionMethod)) {
			return defaultConditionMethod.invoke();
		}
		return null;
	}

	/**
	 * 执行一个condition，如果pool中存在对应的condition，将会调用它的函数执行
	 * 如果不存在，将执行指定的默认方法
	 *
	 * @param condition              需要验证的条件
	 * @param defaultConditionMethod 当条件不存在时需要执行的方法
	 * @return 发发执行返回值
	 */
	public R doIfWithDefault(K condition, ConditionMethod<R> defaultConditionMethod) {
		if (this.functionPool.containsKey(condition)) {
			return functionPool.get(condition).invoke();
		} else {
			return defaultConditionMethod.invoke();
		}
	}
}
