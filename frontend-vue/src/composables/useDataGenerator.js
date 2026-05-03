import { DataGenerator } from '../utils/dataGenerator'

export function useDataGenerator() {
  const generateData = (size, type, distribution, min, max) => {
    return DataGenerator.generateData(size, type, distribution, min, max)
  }

  const generatePersonData = (size, sortField = 'score') => {
    return DataGenerator.generatePersonData(size, sortField)
  }

  const getDistributionOptions = () => DataGenerator.getDistributionOptions()
  const getDataTypeOptions = () => DataGenerator.getDataTypeOptions()
  const getPersonFields = () => DataGenerator.getPersonFields()

  return {
    generateData,
    generatePersonData,
    getDistributionOptions,
    getDataTypeOptions,
    getPersonFields,
  }
}
