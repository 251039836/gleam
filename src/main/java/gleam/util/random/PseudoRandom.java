package gleam.util.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * 简单的伪随机<br>
 * 
 * @author lijr
 * @date 2020-10-19 20:47
 */
public class PseudoRandom {

	private long seed;

	public PseudoRandom(long seed) {
		this.seed = seed;
	}

	/**
	 * 区间随机
	 *
	 * @param min
	 * @param max
	 * @return
	 */
	public int random(int min, int max) {
		if (max <= min) {
			return min;
		}
		return min + random(max - min + 1);
	}

	/**
	 * 伪随机种子，基于线性同余生成器
	 *
	 * @param bound
	 * @return [0, bound)
	 */
	public int random(int bound) {
		this.seed = (this.seed * 9301 + 49297) % 233280;
		double rnd = this.seed / 233280.0;
		double result = Math.floor(rnd * bound);
		return (int) result;
	}

	/**
	 * 随机生成数组<br>
	 * 数组元素在[min,max]中<br>
	 * 若不可重复 且随机范围比数量小 生成的数组小于randomSize
	 *
	 * @param min
	 * @param max
	 * @param randomSize
	 * @param repeat     是否有重复的元素
	 * @return
	 */
	public int[] randomArray(int min, int max, int randomSize, boolean repeat) {
		if (min > max || randomSize <= 0) {
			return new int[0];
		}
		if (repeat) {
			// 可重复抽取
			int[] result = new int[randomSize];
			for (int i = 0; i < randomSize; i++) {
				result[i] = random(min, max);
			}
			return result;
		}
		// 不重复抽取
		int poolSize = max - min + 1;
		if (poolSize < randomSize) {
			randomSize = poolSize;
		}
		int[] srcArray = new int[poolSize];
		for (int i = 0; i < poolSize; i++) {
			srcArray[i] = min + i;
		}
		shuffle(srcArray);
		int[] result = Arrays.copyOf(srcArray, randomSize);
		return result;
	}

	/**
	 * 从一组集合里 抽取多个对象<br>
	 * 几率相同
	 *
	 * @param sourceList
	 * @param resultSize 若数量大于来源 且不可重复 返回列表大小为较小值
	 * @param repeat
	 * @return
	 */
	public <T> List<T> randomList(List<T> sourceList, int resultSize, boolean repeat) {
		if (sourceList == null || sourceList.isEmpty() || resultSize <= 0) {
			return Collections.emptyList();
		}
		int[] randomIndexs = randomArray(0, sourceList.size() - 1, resultSize, repeat);
		List<T> result = new ArrayList<>(resultSize);
		for (int index : randomIndexs) {
			T tmp = sourceList.get(index);
			result.add(tmp);
		}
		return result;
	}

	/**
	 * 从一组集合中 根据权重随机取出1个对象
	 *
	 * @param randomCollection
	 * @param randomSize
	 * @param repeat             是否会重复抽取
	 * @param calcWeightFunction 计算列表元素权重的方法
	 * @return
	 */
	public <T> List<T> randomListByWeight(Collection<T> randomCollection, int randomSize, boolean repeat,
			Function<T, Integer> calcWeightFunction) {
		if (randomCollection == null || randomCollection.isEmpty() || randomSize <= 0 || calcWeightFunction == null) {
			return Collections.emptyList();
		}
		List<T> result = new ArrayList<>();
		int[] weightArray = new int[randomCollection.size()];
		int totalWeight = 0;
		int index = 0;
		for (T tmp : randomCollection) {
			int tmpWeight = calcWeightFunction.apply(tmp);
			totalWeight += tmpWeight;
			weightArray[index++] = tmpWeight;
		}
		if (totalWeight <= 0) {
			return Collections.emptyList();
		}
		for (int i = 0; i < randomSize; i++) {
			if (totalWeight <= 0) {
				break;
			}
			int randomValue = random(totalWeight);
			index = 0;
			for (T tmp : randomCollection) {
				int weight = weightArray[index];
				if (weight <= 0) {
					index++;
					continue;
				}
				randomValue -= weight;
				if (randomValue < 0) {
					result.add(tmp);
					if (!repeat) {
						totalWeight -= weight;
						weightArray[index] = 0;
					}
					break;
				}
				index++;
			}
		}
		return result;
	}

	public <T> Optional<T> randomByWeight(Collection<T> randomCollection, Function<T, Integer> calcWeightFunction) {
		if (randomCollection == null || randomCollection.isEmpty() || calcWeightFunction == null) {
			return Optional.empty();
		}
		int[] weightArray = new int[randomCollection.size()];
		int totalWeight = 0;
		int index = 0;
		for (T tmp : randomCollection) {
			int tmpWeight = calcWeightFunction.apply(tmp);
			totalWeight += tmpWeight;
			weightArray[index++] = tmpWeight;
		}
		if (totalWeight <= 0) {
			return Optional.empty();
		}
		int randomValue = random(totalWeight);
		index = 0;
		for (T tmp : randomCollection) {
			randomValue -= weightArray[index++];
			if (randomValue < 0) {
				return Optional.ofNullable(tmp);
			}
		}
		return Optional.empty();
	}

	/**
	 * 万分比概率
	 * 
	 * @param probability 概率值 （1-10000）
	 * @return true/false
	 */
	public boolean isHit10000(int probability) {
		if (probability < 1) {
			return false;
		}
		if (probability >= 10000) {
			return true;
		}
		int randomValue = random(1, 10000);
		return randomValue < probability;
	}

	/**
	 * 随机打乱数组
	 * 
	 * @param array
	 */
	public void shuffle(int[] array) {
		if (array == null || array.length <= 1) {
			return;
		}
		for (int i = 0; i < array.length; i++) {
			int randomIndex = random(array.length);
			if (randomIndex == i) {
				continue;
			}
			int tmp = array[i];
			array[i] = array[randomIndex];
			array[randomIndex] = tmp;
		}
	}
}
